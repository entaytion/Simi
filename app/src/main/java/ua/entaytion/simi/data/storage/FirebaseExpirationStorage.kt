package ua.entaytion.simi.data.storage

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import ua.entaytion.simi.data.model.ExpirationThreat
import ua.entaytion.simi.utils.ProductMatrix

/**
 * Firebase-based storage for expiration threats. Shared across all devices in real-time.
 */
class FirebaseExpirationStorage(private val context: Context) {
    private val database =
            FirebaseDatabase.getInstance(
                    "https://mrtv-simi-default-rtdb.europe-west1.firebasedatabase.app"
            )

    val threats: Flow<List<ExpirationThreat>> = callbackFlow {
        val settingsStorage = SettingsStorage(context)
        val storeId = settingsStorage.state.first().selectedStoreId
        val threatsRef = database.getReference("expiration_risks/$storeId")

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

                        // Parse list of images
                        val proofImageUrls = mutableListOf<String>()
                        child.child("proofImageUrls").children.forEach { img ->
                            img.getValue(String::class.java)?.let { proofImageUrls.add(it) }
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
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }
        threatsRef.addValueEventListener(listener)
        awaitClose { threatsRef.removeEventListener(listener) }
    }
}
