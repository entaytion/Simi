package ua.entaytion.simi.viewmodel

import android.app.Application
import androidx.annotation.Keep
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.entaytion.simi.data.model.ExpirationReminder
import ua.entaytion.simi.data.storage.FirebaseExpirationStorage
import ua.entaytion.simi.utils.ExpirationDisplayLogic

@Keep
class ExpirationReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = FirebaseExpirationStorage()

    val reminders =
            storage.reminders.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    emptyList()
            )

    fun addReminder(name: String, initialDate: Long?, finalDate: Long) {
        val category = ExpirationDisplayLogic.determineCategory(name)
        val (d10, d25, d50) = ExpirationDisplayLogic.calculateDiscountDates(initialDate, finalDate)

        val item =
                ExpirationReminder(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        category = category.name,
                        initialDate = initialDate,
                        finalDate = finalDate, // This is "Expiration Date"
                        discount10Date = d10,
                        discount25Date = d25,
                        discount50Date = d50
                )
        viewModelScope.launch { storage.addReminder(item) }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch { storage.deleteReminder(id) }
    }

    fun updateReminder(item: ExpirationReminder) {
        viewModelScope.launch { storage.updateReminder(item) }
    }

    fun markDiscountApplied(id: String, discountType: Int) {
        viewModelScope.launch {
            val currentList = reminders.value
            val item = currentList.find { it.id == id } ?: return@launch
            val updatedItem =
                    when (discountType) {
                        10 -> item.copy(isDiscount10Applied = true)
                        25 -> item.copy(isDiscount25Applied = true)
                        50 -> item.copy(isDiscount50Applied = true)
                        else -> item
                    }
            storage.updateReminder(updatedItem)
        }
    }

    fun markWrittenOff(id: String) {
        viewModelScope.launch {
            val currentList = reminders.value
            val item = currentList.find { it.id == id } ?: return@launch
            storage.updateReminder(item.copy(isWrittenOff = true))
        }
    }
}
