package xyz.polyserv.memos.data.model

import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String) {
    SYSTEM("system", "System"),
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский");

    fun toLocale(): Locale? {
        return when (this) {
            SYSTEM -> null
            else -> Locale(code)
        }
    }

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return values().find { it.code == code } ?: SYSTEM
        }
    }
}
