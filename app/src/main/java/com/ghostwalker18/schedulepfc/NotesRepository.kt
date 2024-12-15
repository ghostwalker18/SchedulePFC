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
import java.util.Calendar
import javax.inject.Inject

/**
 * Этот класс представляет репозиторий данных приложения о заметках.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
class NotesRepository(@Inject val db : AppDatabase) {


    /**
     * Этот метод позволяет сохранить заметку.
     */
    fun saveNote(note: Note) {
        db.noteDao().insert(note)
    }

    /**
     * Этот метод позволяет обновить заметку.
     *
     * @param note заметка
     */
    fun updateNote(note: Note) {
        db.noteDao().update(note)
    }

    /**
     * Этот метод позволяет получить заметку по ее ID.
     *
     * @param id первичный ключ
     * @return заметка
     */
    fun getNote(id: Int): LiveData<Note> {
        return db.noteDao().getNote(id)
    }

    /**
     * Этот метод позволяет получить заметки для заданных группы и временного промежутка.
     *
     * @param group группа
     * @param dates список дат для выдачи
     * @return заметки
     */
    fun getNotes(group: String, dates: Array<Calendar>): LiveData<Array<Note>> {
        return if (dates.size == 1) db.noteDao().getNotes(dates[0], group) else db.noteDao()
            .getNotesForDays(dates, group)
    }

    /**
     * Этот метод позволяет получить заметки для заданного ключевого слова и группы.
     *
     * @param group группа
     * @param keyword ключевое слово
     * @return список заметок
     */
    fun getNotes(group: String, keyword: String): LiveData<Array<Note>> {
        return db.noteDao().getNotesByKeyword(keyword, group)
    }

    /**
     * Этот метод позволяет удалить выбранные заметки из БД.
     *
     * @param notes заметки для удаления
     */
    fun deleteNotes(notes: Collection<Note>) {
        for (note in notes) db.noteDao().delete(note)
    }
}