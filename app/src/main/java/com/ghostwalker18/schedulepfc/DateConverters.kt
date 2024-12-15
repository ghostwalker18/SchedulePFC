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

import androidx.room.TypeConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Этот класс используется для ORM.
 * Содержит методы для преобразования Calendar в String для БД и наоборот
 *
 * @author  Ипатов Никита
 * @since 1.0
 */
class DateConverters {
    companion object {
        val dateFormatPhoto = SimpleDateFormat("dd_MM_yyyy",
            Locale("ru"))
        private val dateFormatDb = SimpleDateFormat("dd.MM.yyyy",
            Locale("ru"))

        /**
         * Этот метод используется для преобразования строки в дату согласно заданному формату.
         * @param date строка даты
         * @param format формат даты
         * @return дата
         */
        @Synchronized
        private fun stringToCal(date: String?, format: SimpleDateFormat): Calendar? {
            return date?.let{
                try {
                    val cal = Calendar.getInstance()
                    cal.time = format.parse(it)!!
                    cal
                } catch (e: ParseException) {
                    null
                }
            }
        }
    }

    /**
     * Этот метод преобразует Calendar сущнисти в String для БД.
     * @param date  the entity attribute value to be converted
     * @return
     */
    @TypeConverter
    fun toString(date: Calendar?): String? {
        return date?.let { dateFormatDb.format(it.time)}
    }

    /**
     * Этот метод преобразует String из БД в Calendar сущности.
     * @param date  the data from the database column to be
     *                converted
     * @return
     */
    @TypeConverter
    fun fromString(value: String?): Calendar? {
        return stringToCal(value, dateFormatDb)
    }
}