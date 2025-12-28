package ua.entaytion.simi.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.entaytion.simi.data.model.ChecklistItem
import ua.entaytion.simi.data.storage.ChecklistStorage

data class ChecklistUiState(
        val items: List<ChecklistItem> = emptyList(),
        val isLoading: Boolean = true
)

class ChecklistViewModel(private val storage: ChecklistStorage) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState

    init {
        observeStorage()
    }

    private fun observeStorage() {
        viewModelScope.launch {
            storage.state.collect { persisted ->
                val today = LocalDate.now().toString()
                val baseItems = persisted.items
                val needsBootstrap = persisted.lastResetDate == null
                if (needsBootstrap) {
                    storage.saveState(baseItems, today)
                }
                if (persisted.lastResetDate != today) {
                    val resetItems = baseItems.map { it.copy(isChecked = false) }
                    storage.saveState(resetItems, today)
                    _uiState.value = ChecklistUiState(items = resetItems, isLoading = false)
                } else {
                    _uiState.value = ChecklistUiState(items = baseItems, isLoading = false)
                }
            }
        }
    }

    fun toggleItem(id: Long, checked: Boolean) {
        updateAndPersist { items ->
            items.map { if (it.id == id) it.copy(isChecked = checked) else it }
        }
    }

    fun addItem(title: String) {
        val normalized = title.trim()
        if (normalized.isEmpty()) return
        updateAndPersist { items ->
            items +
                    ChecklistItem(
                            id = System.currentTimeMillis(),
                            title = normalized,
                            isChecked = false
                    )
        }
    }

    fun editItem(id: Long, title: String) {
        val normalized = title.trim()
        if (normalized.isEmpty()) return
        updateAndPersist { items ->
            items.map { if (it.id == id) it.copy(title = normalized) else it }
        }
    }

    fun deleteItem(id: Long) {
        updateAndPersist { items -> items.filterNot { it.id == id } }
    }

    private fun updateAndPersist(transform: (List<ChecklistItem>) -> List<ChecklistItem>) {
        val current = _uiState.value.items
        val updated = transform(current)
        _uiState.value = _uiState.value.copy(items = updated, isLoading = false)
        viewModelScope.launch { storage.saveState(updated) }
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer { ChecklistViewModel(ChecklistStorage(context.applicationContext)) }
        }
    }
}
