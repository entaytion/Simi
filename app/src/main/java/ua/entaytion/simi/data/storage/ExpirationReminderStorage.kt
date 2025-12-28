package ua.entaytion.simi.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ua.entaytion.simi.data.model.ExpirationReminder

private val Context.expirationDataStore by preferencesDataStore(name = "expiration_reminders")

class ExpirationReminderStorage(private val context: Context) {
    private val dataStore = context.expirationDataStore
    private val gson = Gson()
    private val type = object : TypeToken<List<ExpirationReminder>>() {}.type

    val reminders: Flow<List<ExpirationReminder>> =
            dataStore.data.map { prefs ->
                val json = prefs[REMINDERS_KEY]
                if (json.isNullOrBlank()) {
                    emptyList()
                } else {
                    try {
                        gson.fromJson(json, type)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }

    suspend fun addReminder(item: ExpirationReminder) {
        dataStore.edit { prefs ->
            val currentList =
                    try {
                        val json = prefs[REMINDERS_KEY]
                        if (json.isNullOrBlank()) mutableListOf()
                        else gson.fromJson<MutableList<ExpirationReminder>>(json, type)
                    } catch (e: Exception) {
                        mutableListOf()
                    }
            currentList.add(item)
            prefs[REMINDERS_KEY] = gson.toJson(currentList)
        }
    }

    suspend fun updateReminder(item: ExpirationReminder) {
        dataStore.edit { prefs ->
            val currentList =
                    try {
                        val json = prefs[REMINDERS_KEY]
                        if (json.isNullOrBlank()) mutableListOf()
                        else gson.fromJson<MutableList<ExpirationReminder>>(json, type)
                    } catch (e: Exception) {
                        mutableListOf()
                    }
            val index = currentList.indexOfFirst { it.id == item.id }
            if (index != -1) {
                currentList[index] = item
                prefs[REMINDERS_KEY] = gson.toJson(currentList)
            }
        }
    }

    suspend fun deleteReminder(id: String) {
        dataStore.edit { prefs ->
            val currentList =
                    try {
                        val json = prefs[REMINDERS_KEY]
                        gson.fromJson<MutableList<ExpirationReminder>>(json, type)
                                ?: mutableListOf()
                    } catch (e: Exception) {
                        mutableListOf()
                    }
            currentList.removeAll { it.id == id }
            prefs[REMINDERS_KEY] = gson.toJson(currentList)
        }
    }

    companion object {
        private val REMINDERS_KEY = stringPreferencesKey("reminders_list")
    }
}
