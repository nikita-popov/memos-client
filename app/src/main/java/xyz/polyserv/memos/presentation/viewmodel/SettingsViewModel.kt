package xyz.polyserv.memos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.polyserv.memos.data.local.SharedPrefManager
import xyz.polyserv.memos.data.model.AppLanguage
import xyz.polyserv.memos.data.model.ThemeMode
import javax.inject.Inject

data class SettingsUiState(
    val serverUrl: String = "",
    val accessToken: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val isSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.update {
            it.copy(
                serverUrl = sharedPrefManager.getServerUrl(),
                accessToken = sharedPrefManager.getAccessToken() ?: "",
                themeMode = sharedPrefManager.getThemeMode(),
                appLanguage = sharedPrefManager.getAppLanguage()
            )
        }
    }

    fun updateUrl(url: String) {
        _uiState.update { it.copy(serverUrl = url, isSaved = false) }
    }

    fun updateToken(token: String) {
        _uiState.update { it.copy(accessToken = token, isSaved = false) }
    }

    fun updateThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        sharedPrefManager.saveThemeMode(mode)
    }

    fun updateLanguage(language: AppLanguage) {
        _uiState.update { it.copy(appLanguage = language) }
        sharedPrefManager.saveAppLanguage(language)
    }

    fun saveSettings() {
        viewModelScope.launch {
            sharedPrefManager.saveServerUrl(_uiState.value.serverUrl)
            sharedPrefManager.saveAccessToken(_uiState.value.accessToken)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
