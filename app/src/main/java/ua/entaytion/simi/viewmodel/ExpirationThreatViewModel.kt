package ua.entaytion.simi.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ua.entaytion.simi.data.model.ExpirationThreat
import ua.entaytion.simi.utils.ProductMatrix
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class ExpirationThreatViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance("https://mrtv-simi-default-rtdb.europe-west1.firebasedatabase.app")
    private val risksRef = db.getReference("expiration_risks")
    private val storage = FirebaseStorage.getInstance()

    private val _threats = MutableStateFlow<List<ExpirationThreat>>(emptyList())
    val threats: StateFlow<List<ExpirationThreat>> = _threats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        listenToThreats()
    }

    private val _analyzedProduct = MutableStateFlow<ua.entaytion.simi.utils.AnalyzedProduct?>(null)
    val analyzedProduct = _analyzedProduct

    private fun listenToThreats() {
        risksRef.addValueEventListener(object : ValueEventListener {
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
        })
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

    fun addThreat(name: String, matrix: ProductMatrix, date: LocalDate) {
        val id = risksRef.push().key ?: UUID.randomUUID().toString()
        val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val threatMap = mapOf(
            "id" to id,
            "name" to name,
            "matrix" to matrix.name, 
            "expirationDate" to timestamp,
            "isResolved" to false,
            "addedAt" to System.currentTimeMillis()
        )
        
        risksRef.child(id).setValue(threatMap)
    }

    fun updateThreat(threat: ExpirationThreat) {
        val threatMap = mapOf(
            "id" to threat.id,
            "name" to threat.name,
            "matrix" to threat.matrix.name,
            "expirationDate" to threat.expirationDate,
            "isResolved" to threat.isResolved,
            "resolvedAt" to threat.resolvedAt,
            "proofImageUrls" to threat.proofImageUrls,
            "isDiscount10Applied" to threat.isDiscount10Applied,
            "isDiscount25Applied" to threat.isDiscount25Applied,
            "isDiscount50Applied" to threat.isDiscount50Applied
        )
        risksRef.child(threat.id).setValue(threatMap)
    }

    fun applyAction(threatId: String, discountPercent: Int?, markResolved: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updates = mutableMapOf<String, Any>()

                when (discountPercent) {
                    10 -> updates["isDiscount10Applied"] = true
                    25 -> updates["isDiscount25Applied"] = true
                    50 -> updates["isDiscount50Applied"] = true
                }

                if (markResolved) {
                    updates["isResolved"] = true
                    updates["resolvedAt"] = System.currentTimeMillis()
                }

                risksRef.child(threatId).updateChildren(updates).await()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteThreat(threatId: String) {
        risksRef.child(threatId).removeValue()
    }
}
