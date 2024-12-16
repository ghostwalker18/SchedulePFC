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

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject

/**
 * Этот класс используется для отслеживания изменений состояния редактируемой заметки.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
@HiltViewModel
class EditNoteModel @Inject constructor(
    val scheduleRepository: ScheduleRepository,
    val notesRepository: NotesRepository) : ViewModel(){

    private val note = MediatorLiveData<Note>()
    private val noteThemesMediator = MediatorLiveData<Array<String>>()
    private val theme = MutableLiveData("")
    private val text = MutableLiveData("")
    private val photoID = MutableLiveData<Uri?>()
    private val date = MutableLiveData(Calendar.getInstance())
    private val group = MutableLiveData<String>(scheduleRepository.getSavedGroup())
    private var themes: LiveData<Array<String>> = MutableLiveData()
    private var isEdited = false

    init {
        scheduleRepository.getSavedGroup() ?: noteThemesMediator.addSource(
            scheduleRepository.getSubjects(scheduleRepository.getSavedGroup()!!),
            noteThemesMediator::setValue)
    }

    /**
     * Этот метод позволяет задать id заметки для редактирования.
     * @param id идентификатор
     */
    fun setNoteID(id: Int) {
        isEdited = true
        note.addSource(notesRepository.getNote(id), note::setValue)
        note.observeForever {
            if (it != null) {
                group.value = it.group
                date.value = it.date
                text.value = it.text
                theme.value = it.theme
                if (it.photoID != null)
                    photoID.value = Uri.parse(it.photoID)
            }
        }
    }

    /**
     * Этот метод позволяет задать группу для заметки.
     * @param group группа
     */
    fun setGroup(group: String?) {
        this.group.value = group
        noteThemesMediator.removeSource(themes)
        themes = scheduleRepository.getSubjects(group!!)
        noteThemesMediator.addSource(themes, noteThemesMediator::setValue)

    }

    /**
     * Этот метод позволяет получить группу заметки.
     * @return
     */
    fun getGroup(): LiveData<String> {
        return group
    }

    /**
     * Этот метод позволяет получить возможные группы для заметки.
     * @return список допустимых групп
     */
    fun getGroups(): LiveData<Array<String>> {
        return scheduleRepository.getGroups()
    }

    /**
     * Этот метод позволяет задать ID фотографии, прикрепляемой к заметке.
     * @param id uri фотографии
     */
    fun setPhotoID(id: Uri?) {
        photoID.value = id
    }

    /**
     * Этот метод позволяет получить ID фотографии, прикрепленной к заметке.
     * @return
     */
    fun getPhotoID(): LiveData<Uri?> {
        return photoID
    }

    /**
     * Этот метод позволяет задать текст заметки.
     *
     * @param text текст
     */
    fun setText(text: String?) {
        this.text.value = text
    }

    /**
     * Этот метод позволяет получить текст заметки.
     *
     * @return текст
     */
    fun getText(): LiveData<String?> {
        return text
    }

    /**
     * Этот метод позволяет задать тему заметки.
     *
     * @param theme тема
     */
    fun setTheme(theme: String?) {
        this.theme.value = theme
    }

    /**
     * Этот метод позволяет получить тему заметки.
     * @return
     */
    fun getTheme(): LiveData<String?> {
        return theme
    }

    /**
     * Этот метод позволяет получить список предметов у данной группы в качестве тем.
     * @return список предлаагаемых тем
     */
    fun getThemes(): LiveData<Array<String>> {
        return noteThemesMediator
    }

    /**
     * Этот метод позволяет получить текущую дату редактируемой заметки.
     *
     * @return дата
     */
    fun getDate(): LiveData<Calendar?> {
        return date
    }

    /**
     * Этот метод позволяет установить дату редактируемой заметки.
     *
     * @param date дата
     */
    fun setDate(date: Calendar?) {
        this.date.value = date
    }

    /**
     * Этот метод позволяет получить id редактируемой заметки.
     * @return id заметки
     */
    fun getNoteID(): Int {
        return note.value!!.id
    }

    /**
     * Этот метод позволяет сохранить заметку.
     */
    fun saveNote() {
        val noteToSave = note.value
        if (noteToSave != null) {
            noteToSave.date = date.value!!
            noteToSave.group = group.value!!
            noteToSave.theme = theme.value
            noteToSave.text = text.value!!
            if (photoID.value != null) {
                noteToSave.photoID = photoID.value.toString()
                try {
                    ScheduleApp.getInstance().contentResolver.takePersistableUriPermission(
                        photoID.value!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (ignored: Exception) { /*Not required*/}
            } else
                noteToSave.photoID = null
            if (isEdited)
                notesRepository.updateNote(noteToSave)
            else
                notesRepository.saveNote(noteToSave)
        }
    }
}