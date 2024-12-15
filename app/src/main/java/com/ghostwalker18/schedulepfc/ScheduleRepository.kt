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

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

/**
 * Этот класс представляет собой репозиторий данных приложения о расписании.
 *
 * @author  Ипатов Никита
 * @since 1.0
 */
class ScheduleRepository(@Inject val db : AppDatabase,
                         @Inject val api: ScheduleNetworkAPI,
                         @Inject val preferences: SharedPreferences) {

    private val status = MutableLiveData<Status>()
    private val parser: IConverter = DocToLessonsConverter()
    data class Status(val text: String, val progress: Int)

    /**
     * Этот метод возвращает все группы, упоминаемые в расписании.
     *
     * @return список групп
     */
    fun getGroups(): LiveData<Array<String>> {
        return db.lessonDao().getGroups()
    }
}