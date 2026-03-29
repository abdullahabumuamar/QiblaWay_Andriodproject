package com.example.qiblaway2.utils


import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import java.util.Locale

object LanguageHelper {
    fun setLocale(languageCode: String, context: Context) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // حفظ اللغة في SharedPreferences
        val prefs = context.getSharedPreferences("MyPrefs", MODE_PRIVATE)
        prefs.edit().putString("My_Lang", languageCode).apply()
    }

    fun loadLocale(context: Context) {
        val prefs = context.getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "en") ?: "en"
        setLocale(language, context)
    }
}
