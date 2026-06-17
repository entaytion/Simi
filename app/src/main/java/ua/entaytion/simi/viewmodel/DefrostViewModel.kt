package ua.entaytion.simi.viewmodel

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Keep
data class DefrostItem(
    val id: String = "",
    val name: String = "",
    val days: Int = 0
)

val defaultDefrostItems = listOf(
    DefrostItem("default_1", "Тістечко \"Макаронс мікс\" 21г /Nonpareil/", 50),
    DefrostItem("default_2", "Тістечко \"Тарти мікс\" 40г /Nonpareil/", 30),
    DefrostItem("default_3", "Десерт \"Солона карамель\" 100г /Nonpareil/", 16),
    DefrostItem("default_4", "Тістечко Salted Caramel Napoleon", 16),
    DefrostItem("default_5", "Тістечко Caramel (з горіхом)", 16),
    DefrostItem("default_6", "Десерт \"Чизкейк з малиною\" 100г /Nonpareil/", 14),
    DefrostItem("default_7", "Тістечко Raspberry Cheesecake", 14),
    DefrostItem("default_8", "Тістечко Cherry Pincher", 14),
    DefrostItem("default_9", "Вафлі \"Трубочка\" зі згущеним молоком 50г /Мантінга Україна/", 30),
    DefrostItem("default_10", "Торт \"Вафельний\" зі згущеним молоком 45г /Мантінга Україна/", 30),
    DefrostItem("default_11", "Торт \"Чизкейк Нью-Йорк\" 130г /GFS/", 15),
    DefrostItem("default_12", "Мафін \"Шоколадно-банановий\" 80г /Party Box/", 17),
    DefrostItem("default_13", "Мафін \"Тірамісу\" 80г /Party Box/", 17),
    DefrostItem("default_14", "Мафін \"Шоколадний\" 80г /Party Box/", 17),
    DefrostItem("default_15", "Мафін \"Латте\" з маршмелоу 80г /Party Box/", 20),
    DefrostItem("default_16", "Тістечко \"Еклер із заварним кремом\" 50г /Nonpareil/", 8),
    DefrostItem("default_17", "Тістечко \"Еклер зі згущеним молоком\" 50г /Nonpareil/", 8),
    DefrostItem("default_18", "Горішки в ХО (2-6 градусів)", 60)
)

class DefrostViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance(
        "https://mrtv-simi-default-rtdb.europe-west1.firebasedatabase.app"
    )
    private val defrostListRef = database.getReference("defrost_list")

    private val _defrostItems = MutableStateFlow<List<DefrostItem>>(defaultDefrostItems)
    val defrostItems: StateFlow<List<DefrostItem>> = _defrostItems

    private val dbListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val list = mutableListOf<DefrostItem>()
            for (child in snapshot.children) {
                val item = child.getValue(DefrostItem::class.java)
                if (item != null && item.name.isNotBlank()) {
                    list.add(item)
                }
            }
            if (list.isEmpty()) {
                // Якщо Firebase порожній, ініціалізуємо його дефолтним списком
                _defrostItems.value = defaultDefrostItems
                defrostListRef.setValue(defaultDefrostItems)
            } else {
                _defrostItems.value = list
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // У разі помилки лишаємо поточний/дефолтний список
        }
    }

    init {
        defrostListRef.addValueEventListener(dbListener)
    }

    fun addItem(name: String, days: Int) {
        if (name.isBlank() || days <= 0) return
        val current = _defrostItems.value.toMutableList()
        val newItem = DefrostItem(
            id = java.util.UUID.randomUUID().toString(),
            name = name.trim(),
            days = days
        )
        current.add(newItem)
        defrostListRef.setValue(current)
    }

    fun removeItem(id: String) {
        val current = _defrostItems.value.toMutableList()
        if (current.removeAll { it.id == id }) {
            defrostListRef.setValue(current)
        }
    }

    fun updateItem(id: String, name: String, days: Int) {
        if (name.isBlank() || days <= 0) return
        val current = _defrostItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(name = name.trim(), days = days)
            defrostListRef.setValue(current)
        }
    }

    fun resetToDefaults() {
        defrostListRef.setValue(defaultDefrostItems)
    }

    fun updateList(items: List<DefrostItem>) {
        defrostListRef.setValue(items)
    }

    fun moveItemUp(id: String) {
        val current = _defrostItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = current.removeAt(index)
            current.add(index - 1, item)
            defrostListRef.setValue(current)
        }
    }

    fun moveItemDown(id: String) {
        val current = _defrostItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1 && index < current.size - 1) {
            val item = current.removeAt(index)
            current.add(index + 1, item)
            defrostListRef.setValue(current)
        }
    }

    override fun onCleared() {
        super.onCleared()
        defrostListRef.removeEventListener(dbListener)
    }
}
