package com.ghostwalker18.schedulepfc

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Calendar

/**
 * В этом модуле содержаться различные вспомогательные методы, использующиеся по всему приложению.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
@Module
@InstallIn(SingletonComponent::class)
class Utils {
    companion object{
        /**
         * Этот метод используется для проверки, является ли заданная дата сегодняшним днем.
         * @param date дата для проверки
         * @return сегодня ли дата
         */
        fun isDateToday(date: Calendar): Boolean {
            val rightNow = Calendar.getInstance()
            return rightNow[Calendar.YEAR] == date[Calendar.YEAR]
                    && rightNow[Calendar.MONTH] == date[Calendar.MONTH]
                    && rightNow[Calendar.DAY_OF_MONTH] == date[Calendar.DAY_OF_MONTH]
        }

        /**
         * Этот метод используется для генерации даты для заголовка UI элемента.
         * @param date дата
         * @return представление даты в формате ХХ/ХХ
         */
        fun generateDateForTitle(date: Calendar): String {
            //Month is a number in 0 - 11
            val month = date[Calendar.MONTH] + 1
            //Formatting month number with leading zero
            var monthString = month.toString()
            if (month < 10) {
                monthString = "0$monthString"
            }
            val day = date[Calendar.DAY_OF_MONTH]
            var dayString = day.toString()
            //Formatting day number with leading zero
            if (day < 10) {
                dayString = "0$dayString"
            }
            return "$dayString/$monthString"
        }

        /**
         * Этот метод позволяет получить имя скачиваемого файла из ссылки на него.
         *
         * @param link ссылка на файл
         * @return имя файла
         */
        fun getNameFromLink(link: String): String {
            val parts = link.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            return parts[parts.size - 1]
        }
    }

    /**
     * Этот метод поставляет компонентам SharedPreferences приложения.
     */
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}