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
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject

/**
 * Этот класс служит для отслеживания изменений состояния списка заметок, отображаемого пользователю.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
@HiltViewModel
class NotesModel @Inject constructor(
    val repository: NotesRepository,
    val scheduleRepository: ScheduleRepository): ViewModel(){

    private val notes = MediatorLiveData<Array<Note>>()
    private var notesMediator: LiveData<Array<Note>> = MutableLiveData()
    private val startDate = MutableLiveData(Calendar.getInstance())
    private val endDate = MutableLiveData(Calendar.getInstance())
    private var group: String? = null
    private var keyword: String? = null

    init {
        setGroup(scheduleRepository.getSavedGroup())
    }

    /**
     * Этот метод выдает заметки для заданнной группы и временного интервала.
     * @return список заметок
     */
    fun getNotes(): LiveData<Array<Note>?> {
        return notes
    }

    /**
     * Этот метод позволяет получить стартовую дату временного интервала для выдачи заметок.
     * @return стратовая дата
     */
    fun getStartDate(): LiveData<Calendar?> {
        return startDate
    }

    /**
     * Этот метод позволяет получить конечную дату временного интервала для выдачи заметок.
     * @return конечная дата
     */
    fun getEndDate(): LiveData<Calendar?> {
        return endDate
    }

    /**
     * Этот метод задает группу для выдачи заметок.
     * @param group группа
     */
    fun setGroup(group: String?) {
        this.group = group
        notes.removeSource(notesMediator)
        if (group != null) {
            if (keyword != null) notesMediator = repository.getNotes(group, keyword!!)
            if (startDate.value != null && endDate.value != null)
                notesMediator = repository
                    .getNotes(group, generateDateSequence(startDate.value!!, endDate.value!!))
        }
        notes.addSource(notesMediator){ x -> notes.setValue(x) }
    }

    /**
     * Этот метод позволяет получить группу для выдачи заметок
     * @return группа
     */
    fun getGroup(): String? {
        return group
    }

    /**
     * Этот метод задает ключевое слова для поиска заметок по нему и выдачи их.
     * @param keyword ключевое слово
     */
    fun setKeyword(keyword: String?) {
        this.keyword = keyword
        notes.removeSource(notesMediator)
        if (keyword != null) notesMediator = repository.getNotes(group!!, keyword) else {
            if (startDate.value != null && endDate.value != null && group != null) notesMediator =
                repository.getNotes(
                    group!!,
                    generateDateSequence(startDate.value!!, endDate.value!!)
                )
        }
        notes.addSource(notesMediator, notes::setValue)
    }

    /**
     * Этот метод устанавливает начальную дату временного интервала выдачи заметок.
     * @param date начальная дата
     */
    fun setStartDate(date: Calendar?) {
        startDate.value = date
        notes.removeSource(notesMediator)
        if (startDate.value != null && endDate.value != null && group != null)
            notesMediator = repository
                .getNotes(group!!, generateDateSequence(startDate.value!!, endDate.value!!))
        notes.addSource(notesMediator, notes::setValue)
    }

    /**
     * Этот метод устанавливает конечную дату временного интервала выдачи заметок.
     * @param date конечная дата
     */
    fun setEndDate(date: Calendar?) {
        endDate.value = date
        notes.removeSource(notesMediator)
        if (startDate.value != null && endDate.value != null && group != null)
            notesMediator = repository
                .getNotes(group!!, generateDateSequence(startDate.value!!, endDate.value!!))
        notes.addSource(notesMediator, notes::setValue)
    }

    /**
     * Этот метод позволяет получить последовательность дат, основываясь на начальной и конечной.
     * @param startDate начальная дата (включается в интервал)
     * @param endDate конечная дата (включается в интервал)
     * @return массив дат
     */
    private fun generateDateSequence(startDate: Calendar, endDate: Calendar): Array<Calendar> {
        if (startDate == endDate || endDate.before(startDate)) return arrayOf(startDate)
        val resultList: MutableList<Calendar> = ArrayList()
        //remember of reference nature of Java
        val counter = startDate.clone() as Calendar
        while (counter.before(endDate)) {
            //remember of reference nature of Java (also here)
            val date = counter.clone() as Calendar
            resultList.add(date)
            counter.add(Calendar.DATE, 1)
        }
        return resultList.toTypedArray()
    }
}