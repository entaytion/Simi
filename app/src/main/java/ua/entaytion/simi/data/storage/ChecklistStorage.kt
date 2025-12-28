package ua.entaytion.simi.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ua.entaytion.simi.data.model.ChecklistItem

private val Context.checklistDataStore by preferencesDataStore(name = "checklist")

data class ChecklistPersistedState(
    val items: List<ChecklistItem>,
    val lastResetDate: String?
)

class ChecklistStorage(private val context: Context) {

    private val dataStore = context.checklistDataStore

    val state: Flow<ChecklistPersistedState> = dataStore.data.map { prefs ->
        val stored = prefs[TASKS_KEY]
        val parsed = decode(stored)
        ChecklistPersistedState(
            items = if (parsed.isEmpty()) defaultItems() else parsed,
            lastResetDate = prefs[LAST_RESET_KEY]
        )
    }

    suspend fun saveState(items: List<ChecklistItem>, resetDate: String? = null) {
        dataStore.edit { prefs ->
            prefs[TASKS_KEY] = encode(items)
            resetDate?.let { prefs[LAST_RESET_KEY] = it }
        }
    }

    fun defaultItems(): List<ChecklistItem> = DefaultChecklistTitles.mapIndexed { index, title ->
        ChecklistItem(
            id = (index + 1L),
            title = title,
            isChecked = false
        )
    }

    private fun decode(raw: String?): List<ChecklistItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(ITEM_SEPARATOR)
            .mapNotNull { token ->
                val parts = token.split(FIELD_SEPARATOR)
                if (parts.size != 3) return@mapNotNull null
                val id = parts[0].toLongOrNull() ?: return@mapNotNull null
                val title = parts[1]
                val checked = parts[2].toBooleanStrictOrNull() ?: return@mapNotNull null
                ChecklistItem(id = id, title = title, isChecked = checked)
            }
    }

    private fun encode(items: List<ChecklistItem>): String =
        items.joinToString(separator = ITEM_SEPARATOR) { item ->
            listOf(item.id, item.title, item.isChecked).joinToString(FIELD_SEPARATOR)
        }

    companion object {
        private val TASKS_KEY = stringPreferencesKey("tasks")
        private val LAST_RESET_KEY = stringPreferencesKey("last_reset")
        private const val ITEM_SEPARATOR = "||"
        private const val FIELD_SEPARATOR = "::"

        private val DefaultChecklistTitles = listOf(
            "Помити відсіки зі слашем",
            "Помити відсік з bubble drink",
            "Помити гриль",
            "Досипати каву",
            "Перевірити хліб та обрати \"повернення\"",
            "Перевірити просрочку та обрати \"протермін\"",
            "Перевірити багети",
            "Перерахувати касу",
            "Надрукувати X-звіт та Z-звіт",
            "Сфотографувати кількість продажів",
            "Сфотографувати суму виторгу"
        )
    }
}

private fun String.toBooleanStrictOrNull(): Boolean? = when (this.lowercase()) {
    "true" -> true
    "false" -> false
    else -> null
}
