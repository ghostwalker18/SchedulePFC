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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.Date

/**
 * Этот класс используется для отслеживания изменения состояния расписания.
 *
 * @author  Ипатов Никита
 * @since 1.0
 */
class ScheduleState : ViewModel(){
    private val group = MutableLiveData<String>()
    private val teacher = MutableLiveData<String>()
    private val calendar = MutableLiveData(
        Calendar.Builder()
            .setInstant(Date())
            .build()
    )

    /**
     * Этот метод позволяет передвинуть состояние расписания на следующую неделю.
     */
    fun goNextWeek() {
        val date = calendar.value
        date!!.add(Calendar.WEEK_OF_YEAR, 1)
        calendar.value = date
    }

    /**
     * Этот метод позволяет передвинуть состояние расписания на предыдущую неделю.
     */
    fun goPreviousWeek() {
        val date = calendar.value
        date!!.add(Calendar.WEEK_OF_YEAR, -1)
        calendar.value = date
    }

    fun getCalendar(): LiveData<Calendar?> {
        return calendar
    }

    fun getYear(): Int {
        return calendar.value!![Calendar.YEAR]
    }

    fun getWeek(): Int {
        return calendar.value!![Calendar.WEEK_OF_YEAR]
    }

    fun setGroup(group: String?) {
        this.group.value = group
    }

    fun getGroup(): LiveData<String?> {
        return group
    }

    fun setTeacher(teacher: String?) {
        this.teacher.value = teacher
    }

    fun getTeacher(): LiveData<String?> {
        return teacher
    }
}