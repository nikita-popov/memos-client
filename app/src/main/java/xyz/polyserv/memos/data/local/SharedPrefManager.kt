package xyz.polyserv.memos.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import xyz.polyserv.memos.data.model.AppLanguage
import xyz.polyserv.memos.data.model.ThemeMode
import androidx.core.content.edit

@Singleton
class SharedPrefManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("memos_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val DEFAULT_URL = ""
    }

    fun saveServerUrl(url: String) {
        val cleanUrl = if (url.endsWith("/")) url.dropLast(1) else url
        prefs.edit { putString(KEY_SERVER_URL, cleanUrl) }
    }

    fun getServerUrl(): String {
        return prefs.getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    fun saveAccessToken(token: String) {
        prefs.edit { putString(KEY_ACCESS_TOKEN, token) }
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
    }

    fun getThemeMode(): ThemeMode {
        val mode = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return ThemeMode.fromString(mode)
    }

    fun saveAppLanguage(language: AppLanguage) {
        prefs.edit { putString(KEY_APP_LANGUAGE, language.code) }
    }

    fun getAppLanguage(): AppLanguage {
        val code = prefs.getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.code)
        return AppLanguage.fromCode(code)
    }
}
