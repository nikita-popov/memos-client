package xyz.polyserv.memos.util

import android.app.Activity
import android.content.res.Configuration
import java.util.Locale
import xyz.polyserv.memos.data.model.AppLanguage

object LocaleHelper {

    fun setLocale(activity: Activity, language: AppLanguage) {
        val locale = language.toLocale() ?: getSystemLocale()
        Locale.setDefault(locale)

        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)

        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    private fun getSystemLocale(): Locale {
        return Locale.getDefault(Locale.Category.DISPLAY)
    }

    fun getDisplayName(language: AppLanguage, currentLocale: Locale): String {
        return when (language) {
            AppLanguage.SYSTEM -> "System"
            else -> language.displayName
        }
    }
}
