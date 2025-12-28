package ua.entaytion.simi.viewmodel

import android.app.Application
import androidx.annotation.Keep
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.entaytion.simi.data.model.UserMode
import ua.entaytion.simi.data.storage.SettingsStorage

@Keep
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = SettingsStorage(application)

    val settingsState =
            storage.state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch { storage.setDarkTheme(isDark) }
    }

    fun setUserMode(mode: UserMode) {
        viewModelScope.launch { storage.setUserMode(mode) }
    }
}
