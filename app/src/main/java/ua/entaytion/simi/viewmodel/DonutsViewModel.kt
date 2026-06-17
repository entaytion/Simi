package ua.entaytion.simi.viewmodel

import androidx.annotation.Keep
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

@Keep
data class DonutEntry(val name: String, val dateTaken: LocalDateTime, val count: Int)

class DonutsViewModel : ViewModel() {
    private val _entries = mutableStateListOf<DonutEntry>()
    val entries: List<DonutEntry>
        get() = _entries

    val maxCountPerItem = 6

    // Експліцитно вказуємо URL бази Firebase RTDB
    private val database = FirebaseDatabase.getInstance(
        "https://mrtv-simi-default-rtdb.europe-west1.firebasedatabase.app"
    )
    private val donutListRef = database.getReference("donut_list")

    val defaultDonutNames = listOf(
        "Чорниця",
        "Кіт-Кат",
        "Мандарин нектарин",
        "Pink з полуницею",
        "Pinkie з маршмелоу",
        "Cookies",
        "Конфеті Кенді",
        "Very cremy berry",
        "Rafaela",
        "Капучино",
        "Панакота",
        "Яблуко-кориця",
        "Карамелька",
        "Потрійний шоколад",
        "Вишневі обійми",
        "Малинова ніжність",
        "Лісовий горіх",
        "Вафлі \"Трубочка\"",
        "Торт \"Вафельний\"",
        "Мафін \"Тірамісу\"",
        "Мафін \"Шоколадний\"",
        "Мафін \"Вишневий\"",
        "Еклер зі згущеним молоком",
        "Еклер з заварним кремом",
        "Чізкейк \"Нью-Йорк\"",
        "Чізкейк \"Вишня (Сакура)\"",
        "Чізкейк \"Солона карамель\""
    ).distinct()

    private val _donutNames = MutableStateFlow<List<String>>(defaultDonutNames)
    val donutNames: StateFlow<List<String>> = _donutNames

    private val dbListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val list = mutableListOf<String>()
            for (child in snapshot.children) {
                val name = child.getValue(String::class.java)
                if (!name.isNullOrBlank()) {
                    list.add(name)
                }
            }
            if (list.isEmpty()) {
                // Якщо Firebase порожній, ініціалізуємо його дефолтним списком
                _donutNames.value = defaultDonutNames
                donutListRef.setValue(defaultDonutNames)
            } else {
                _donutNames.value = list.distinct()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // У разі помилки лишаємо поточний/дефолтний список
        }
    }

    init {
        donutListRef.addValueEventListener(dbListener)
    }

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

    fun addDonutName(name: String) {
        val current = _donutNames.value.toMutableList()
        if (name.isNotBlank() && !current.contains(name)) {
            current.add(name)
            donutListRef.setValue(current)
        }
    }

    fun removeDonutName(name: String) {
        val current = _donutNames.value.toMutableList()
        if (current.remove(name)) {
            donutListRef.setValue(current)
        }
    }

    fun renameDonutName(oldName: String, newName: String) {
        if (newName.isBlank() || oldName == newName) return
        val current = _donutNames.value.toMutableList()
        val index = current.indexOf(oldName)
        if (index != -1) {
            current[index] = newName
            donutListRef.setValue(current)
        }
    }

    fun resetDonutNames() {
        donutListRef.setValue(defaultDonutNames)
    }

    fun updateDonutNames(names: List<String>) {
        donutListRef.setValue(names)
    }

    override fun onCleared() {
        super.onCleared()
        donutListRef.removeEventListener(dbListener)
    }
}
