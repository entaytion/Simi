package ua.entaytion.simi.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import ua.entaytion.simi.data.model.ExpirationThreat
import ua.entaytion.simi.data.storage.SettingsStorage
import ua.entaytion.simi.utils.ProductMatrix
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class ExpirationThreatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseDatabase.getInstance("https://mrtv-simi-default-rtdb.europe-west1.firebasedatabase.app")
    private val storage = FirebaseStorage.getInstance()
    private val settingsStorage = SettingsStorage(application)

    private val _threats = MutableStateFlow<List<ExpirationThreat>>(emptyList())
    val threats: StateFlow<List<ExpirationThreat>> = _threats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentStoreId: String? = null
    private var dbListener: ValueEventListener? = null

    init {
        viewModelScope.launch {
            settingsStorage.state.collect { settings ->
                val newStoreId = settings.selectedStoreId
                if (newStoreId != currentStoreId) {
                    // Unsubscribe old listener
                    currentStoreId?.let { oldId ->
                        dbListener?.let { listener ->
                            db.getReference("expiration_risks/$oldId").removeEventListener(listener)
                        }
                    }
                    currentStoreId = newStoreId
                    listenToThreats(newStoreId)
                }
            }
        }
    }

    private val _analyzedProduct = MutableStateFlow<ua.entaytion.simi.utils.AnalyzedProduct?>(null)
    val analyzedProduct = _analyzedProduct

    private fun getRisksRef(): DatabaseReference {
        val storeId = currentStoreId ?: "6102"
        return db.getReference("expiration_risks/$storeId")
    }

    private fun listenToThreats(storeId: String) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ExpirationThreat>()
                for (child in snapshot.children) {
                    try {
                        val id = child.child("id").getValue(String::class.java) ?: ""
                        val name = child.child("name").getValue(String::class.java) ?: ""
                        val matrixStr = child.child("matrix").getValue(String::class.java) ?: "FRESH"
                        val expirationDate = child.child("expirationDate").getValue(Long::class.java) ?: 0L
                        val isResolved = child.child("isResolved").getValue(Boolean::class.java) ?: false
                        
                        // Parse list of images
                        val proofImageUrls = mutableListOf<String>()
                        child.child("proofImageUrls").children.forEach { img ->
                            img.getValue(String::class.java)?.let { proofImageUrls.add(it) }
                        }
                        
                        val resolvedAt = child.child("resolvedAt").getValue(Long::class.java)
                        val addedAt = child.child("addedAt").getValue(Long::class.java) ?: System.currentTimeMillis()
                        
                        val d10 = child.child("isDiscount10Applied").getValue(Boolean::class.java) ?: false
                        val d25 = child.child("isDiscount25Applied").getValue(Boolean::class.java) ?: false
                        val d50 = child.child("isDiscount50Applied").getValue(Boolean::class.java) ?: false

                        val matrix = try {
                            ProductMatrix.valueOf(matrixStr)
                        } catch (e: Exception) {
                            ProductMatrix.FRESH
                        }

                        list.add(ExpirationThreat(
                            id = id,
                            name = name,
                            matrix = matrix,
                            expirationDate = expirationDate,
                            isResolved = isResolved,
                            proofImageUrls = proofImageUrls,
                            resolvedAt = resolvedAt,
                            addedAt = addedAt,
                            isDiscount10Applied = d10,
                            isDiscount25Applied = d25,
                            isDiscount50Applied = d50
                        ))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // Sort: Unresolved first, then by date
                _threats.value = list.sortedWith(
                    compareBy<ExpirationThreat> { it.isResolved }
                        .thenBy { it.expirationDate }
                )
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error
            }
        }
        dbListener = listener
        db.getReference("expiration_risks/$storeId").addValueEventListener(listener)
    }
    
    fun analyzeImage(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = ua.entaytion.simi.utils.AIService.analyzeProductFromImage(bitmap)
                _analyzedProduct.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearAnalyzedResult() {
        _analyzedProduct.value = null
    }

    suspend fun uploadImage(uri: Uri, context: android.content.Context): String {
        val ref = storage.reference.child("threat_proofs/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    fun addThreat(name: String, matrix: ProductMatrix, date: LocalDate, imageUris: List<Uri> = emptyList(), context: android.content.Context? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imageUrls = mutableListOf<String>()
                if (context != null && imageUris.isNotEmpty()) {
                    for (uri in imageUris) {
                        try {
                            val url = uploadImage(uri, context)
                            imageUrls.add(url)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Помилка завантаження фото: ${e.localizedMessage}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                
                val currentRef = getRisksRef()
                val id = currentRef.push().key ?: UUID.randomUUID().toString()
                val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                val threatMap = mapOf(
                    "id" to id,
                    "name" to name,
                    "matrix" to matrix.name, 
                    "expirationDate" to timestamp,
                    "isResolved" to false,
                    "addedAt" to System.currentTimeMillis(),
                    "proofImageUrls" to imageUrls
                )
                
                currentRef.child(id).setValue(threatMap).await()
            } catch (e: Exception) {
                e.printStackTrace()
                if (context != null) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            context,
                            "Помилка бази даних: ${e.localizedMessage}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateThreat(threat: ExpirationThreat, newImageUris: List<Uri> = emptyList(), context: android.content.Context? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imageUrls = threat.proofImageUrls.toMutableList()
                if (context != null && newImageUris.isNotEmpty()) {
                    for (uri in newImageUris) {
                        try {
                            val url = uploadImage(uri, context)
                            imageUrls.add(url)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Помилка завантаження фото: ${e.localizedMessage}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                
                val threatMap = mapOf(
                    "id" to threat.id,
                    "name" to threat.name,
                    "matrix" to threat.matrix.name,
                    "expirationDate" to threat.expirationDate,
                    "isResolved" to threat.isResolved,
                    "resolvedAt" to threat.resolvedAt,
                    "proofImageUrls" to imageUrls,
                    "isDiscount10Applied" to threat.isDiscount10Applied,
                    "isDiscount25Applied" to threat.isDiscount25Applied,
                    "isDiscount50Applied" to threat.isDiscount50Applied
                )
                getRisksRef().child(threat.id).setValue(threatMap).await()
            } catch (e: Exception) {
                e.printStackTrace()
                if (context != null) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            context,
                            "Помилка оновлення: ${e.localizedMessage}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyAction(threatId: String, discountPercent: Int?, markResolved: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentRef = getRisksRef()
                if (markResolved) {
                    currentRef.child(threatId).removeValue().await()
                } else {
                    val updates = mutableMapOf<String, Any>()
                    when (discountPercent) {
                        10 -> updates["isDiscount10Applied"] = true
                        25 -> updates["isDiscount25Applied"] = true
                        50 -> updates["isDiscount50Applied"] = true
                    }
                    if (updates.isNotEmpty()) {
                        currentRef.child(threatId).updateChildren(updates).await()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteThreat(threatId: String) {
        getRisksRef().child(threatId).removeValue()
    }

    override fun onCleared() {
        super.onCleared()
        currentStoreId?.let { storeId ->
            dbListener?.let { listener ->
                db.getReference("expiration_risks/$storeId").removeEventListener(listener)
            }
        }
    }
}
