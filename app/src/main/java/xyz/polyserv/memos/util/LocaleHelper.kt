package xyz.polyserv.memos.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale.getDefault(Locale.Category.DISPLAY)
        } else {
            Locale.getDefault()
        }
    }

    fun getDisplayName(language: AppLanguage, currentLocale: Locale): String {
        return when (language) {
            AppLanguage.SYSTEM -> "System"
            else -> language.displayName
        }
    }
}
