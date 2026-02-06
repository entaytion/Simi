package ua.entaytion.simi.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime

enum class HotDogType {
    DeFrost,
    Container
}

data class HotDogEntry(
        val name: String,
        val type: HotDogType,
        val ptp: LocalDateTime,
        val ktp: LocalDateTime
)

class HotDogsViewModel : ViewModel() {
    private val _entries = mutableStateListOf<HotDogEntry>()
    val entries: List<HotDogEntry>
        get() = _entries

    fun addEntry(name: String, type: HotDogType) {
        val count = _entries.count { it.name == name && it.type == type }
        val limit =
                when (type) {
                    HotDogType.DeFrost -> 2
                    HotDogType.Container -> 1
                }

        if (count >= limit) return

        val ptp = LocalDateTime.now()
        val days =
                when (type) {
                    HotDogType.DeFrost -> {
                        if (name.contains("Швейцарські", true) || name.contains("італійські", true))
                                5
                        else if (name.contains("булка", true)) 1 else 2
                    }
                    HotDogType.Container -> {
                        if (name.contains("італійські", true)) 2 else 1
                    }
                }

        // KTP is +days, same time minus 1 minute
        val ktp = ptp.plusDays(days.toLong()).minusMinutes(1)

        _entries.add(0, HotDogEntry(name, type, ptp, ktp))
    }

    fun removeEntry(entry: HotDogEntry) {
        _entries.remove(entry)
    }
}
