package ua.entaytion.simi.data.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ua.entaytion.simi.data.model.UserMode

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class SettingsState(
    val isDarkTheme: Boolean,
    val userMode: UserMode,
    val selectedStoreId: String,
    val selectedStoreName: String,
    val showBakingAll: Boolean
)

class SettingsStorage(private val context: Context) {
    private val dataStore = context.settingsDataStore

    val state: Flow<SettingsState> =
            dataStore.data.map { prefs ->
                val isDark = prefs[DARK_THEME_KEY] ?: true // Default dark
                val modeName = prefs[USER_MODE_KEY] ?: UserMode.NEWBIE.name
                val storeId = prefs[SELECTED_STORE_KEY] ?: "6102"
                val storeName = prefs[SELECTED_STORE_NAME_KEY] ?: "Хмельницький, Львівське шосе, 21/2"
                val showBakingAll = prefs[SHOW_BAKING_ALL_KEY] ?: false
                val mode =
                        try {
                            UserMode.valueOf(modeName)
                        } catch (e: Exception) {
                            UserMode.NEWBIE
                        }
                SettingsState(isDark, mode, storeId, storeName, showBakingAll)
            }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { it[DARK_THEME_KEY] = isDark }
    }

    suspend fun setUserMode(mode: UserMode) {
        dataStore.edit { it[USER_MODE_KEY] = mode.name }
    }

    suspend fun setSelectedStore(storeId: String, storeName: String) {
        dataStore.edit {
            it[SELECTED_STORE_KEY] = storeId
            it[SELECTED_STORE_NAME_KEY] = storeName
        }
    }

    suspend fun setShowBakingAll(showBakingAll: Boolean) {
        dataStore.edit { it[SHOW_BAKING_ALL_KEY] = showBakingAll }
    }

    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")
        private val USER_MODE_KEY = stringPreferencesKey("user_mode")
        private val SELECTED_STORE_KEY = stringPreferencesKey("selected_store_id")
        private val SELECTED_STORE_NAME_KEY = stringPreferencesKey("selected_store_name")
        private val SHOW_BAKING_ALL_KEY = booleanPreferencesKey("show_baking_all")
    }
}
