/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.ghostwalker18.schedulepfc

import android.app.Application
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

/**
 * <h1>SchedulePATC</h1>
 * <p>
 *      Программа представляет собой мобильную реализацию приложения расписания ПЛТТ.
 * </p>
 *
 * @author  Ипатов Никита
 * @since  1.0
 */
@HiltAndroidApp
class ScheduleApp: Application(), OnSharedPreferenceChangeListener {
    @Inject lateinit var preferences: SharedPreferences
    @Inject lateinit var database: AppDatabase
    @Inject lateinit var scheduleRepository: ScheduleRepository
    @Inject lateinit var notesRepository: NotesRepository

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key){
            "theme" -> {
                val theme = sharedPreferences?.getString(key, "")
                setTheme(theme)
            }
            "language" -> {
                val localeCode = sharedPreferences?.getString(key, "en")
                setLocale(localeCode)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        instance = this
        val theme = preferences.getString("theme", "")
        setTheme(theme)
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Этот метод позволяет установить язык приложения
     * @param localeCode код языка
     */
    private fun setLocale(localeCode : String?) {
        val localeListCompat = if (localeCode == "system")
            LocaleListCompat.getEmptyLocaleList()
        else
            LocaleListCompat.create(localeCode?.let { Locale(it) })
        AppCompatDelegate.setApplicationLocales(localeListCompat)
    }
    /**
     * Этот метод позволяет установить тему приложения
     * @param theme код темы (system, day, night)
     */
    private fun setTheme(theme : String?){
        when(theme){
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "night" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "day" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    companion object {
        const val baseUri = "https://patt.karelia.ru"
        private lateinit var instance: ScheduleApp

        /**
         * Этот метод позволяет получить доступ к экзэмпляру приложения
         * @return приложение
         */
        @JvmStatic
        fun getInstance(): ScheduleApp {
            return instance
        }
    }
}