package ua.entaytion.simi.data.storage

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import ua.entaytion.simi.data.model.ExpirationReminder

/**
 * Firebase-based storage for expiration reminders. All devices share the same list in real-time.
 */
class FirebaseExpirationStorage {
    // Explicitly specify database URL to ensure all devices use the same database
    private val database =
            FirebaseDatabase.getInstance(
                    "https://mrtv-simi-default-rtdb.europe-west1.firebasedatabase.app"
            )
    private val remindersRef = database.getReference("expiration_reminders")

    /**
     * Real-time flow of all reminders from Firebase. Updates automatically when data changes on any
     * device.
     */
    val reminders: Flow<List<ExpirationReminder>> = callbackFlow {
        val listener =
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list = mutableListOf<ExpirationReminder>()
                        for (child in snapshot.children) {
                            try {
                                val reminder =
                                        child.getValue(ExpirationReminderFirebase::class.java)
                                if (reminder != null) {
                                    list.add(reminder.toExpirationReminder(child.key ?: ""))
                                }
                            } catch (e: Exception) {
                                // Skip invalid entries
                            }
                        }
                        trySend(list)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // On error, send empty list
                        trySend(emptyList())
                    }
                }

        remindersRef.addValueEventListener(listener)

        awaitClose { remindersRef.removeEventListener(listener) }
    }

    suspend fun addReminder(item: ExpirationReminder) {
        val firebaseItem = ExpirationReminderFirebase.fromExpirationReminder(item)
        remindersRef.child(item.id).setValue(firebaseItem).await()
    }

    suspend fun updateReminder(item: ExpirationReminder) {
        val firebaseItem = ExpirationReminderFirebase.fromExpirationReminder(item)
        remindersRef.child(item.id).setValue(firebaseItem).await()
    }

    suspend fun deleteReminder(id: String) {
        remindersRef.child(id).removeValue().await()
    }
}

/**
 * Firebase-compatible data class (needs default constructor and mutable properties for Firebase)
 */
data class ExpirationReminderFirebase(
        val name: String = "",
        val category: String = "FRESH",
        val initialDate: Long? = null,
        val finalDate: Long = 0L,
        val discount10Date: Long? = null,
        val discount25Date: Long? = null,
        val discount50Date: Long? = null,
        val isDiscount10Applied: Boolean = false,
        val isDiscount25Applied: Boolean = false,
        val isDiscount50Applied: Boolean = false,
        val isWrittenOff: Boolean = false
) {
    fun toExpirationReminder(id: String) =
            ExpirationReminder(
                    id = id,
                    name = name,
                    category = category,
                    initialDate = initialDate,
                    finalDate = finalDate,
                    discount10Date = discount10Date,
                    discount25Date = discount25Date,
                    discount50Date = discount50Date,
                    isDiscount10Applied = isDiscount10Applied,
                    isDiscount25Applied = isDiscount25Applied,
                    isDiscount50Applied = isDiscount50Applied,
                    isWrittenOff = isWrittenOff
            )

    companion object {
        fun fromExpirationReminder(item: ExpirationReminder) =
                ExpirationReminderFirebase(
                        name = item.name,
                        category = item.category,
                        initialDate = item.initialDate,
                        finalDate = item.finalDate,
                        discount10Date = item.discount10Date,
                        discount25Date = item.discount25Date,
                        discount50Date = item.discount50Date,
                        isDiscount10Applied = item.isDiscount10Applied,
                        isDiscount25Applied = item.isDiscount25Applied,
                        isDiscount50Applied = item.isDiscount50Applied,
                        isWrittenOff = item.isWrittenOff
                )
    }
}
