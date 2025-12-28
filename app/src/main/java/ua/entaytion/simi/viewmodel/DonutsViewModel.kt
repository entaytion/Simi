package ua.entaytion.simi.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime

data class DonutEntry(val name: String, val dateTaken: LocalDateTime, val count: Int)

class DonutsViewModel : ViewModel() {
    private val _entries = mutableStateListOf<DonutEntry>()
    val entries: List<DonutEntry>
        get() = _entries

    val maxCountPerItem = 6

    fun upsert(name: String, delta: Int) {
        if (name.isBlank()) return
        val idx = _entries.indexOfFirst { it.name == name }
        if (idx == -1) {
            if (delta > 0) {
                _entries.add(
                        DonutEntry(
                                name = name,
                                dateTaken = LocalDateTime.now(),
                                count = delta.coerceIn(1, maxCountPerItem)
                        )
                )
            }
            return
        }
        val current = _entries[idx]
        val next = (current.count + delta).coerceAtMost(maxCountPerItem)
        if (next <= 0) {
            _entries.removeAt(idx)
        } else {
            _entries[idx] = current.copy(count = next)
        }
    }
}
