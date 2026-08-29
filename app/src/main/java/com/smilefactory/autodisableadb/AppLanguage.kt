package com.smilefactory.autodisableadb

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object AppLanguage {
    const val CHINESE = "zh-TW"
    const val ENGLISH = "en"

    fun isEnglish(): Boolean {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val tag = if (appLocales.isEmpty) {
            Locale.getDefault().toLanguageTag()
        } else {
            appLocales.toLanguageTags()
        }
        return tag.startsWith("en", ignoreCase = true)
    }

    fun toggle() {
        val next = if (isEnglish()) CHINESE else ENGLISH
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next))
    }
}
