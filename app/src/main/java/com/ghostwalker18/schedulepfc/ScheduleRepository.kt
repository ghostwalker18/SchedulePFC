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

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Calendar
import java.util.concurrent.Callable
import javax.inject.Inject

/**
 * Этот класс представляет собой репозиторий данных приложения о расписании.
 *
 * @author  Ипатов Никита
 * @since 1.0
 */
class ScheduleRepository @Inject constructor(@ApplicationContext val context: Context,
                          val db : AppDatabase,
                          val api: ScheduleNetworkAPI,
                          val preferences: SharedPreferences) {

    private val status = MutableLiveData<Status>()
    private val parser: IConverter = DocToLessonsConverter()
    data class Status(val text: String, val progress: Int)

    /**
     * Этот метод обновляет репозиторий приложения.
     * Метод использует многопоточность и может вызывать исключения в других потоках.
     * Требуется интернет соединение.
     */
    fun update(){
    }

    /**
     * Этот метод используется для получения состояния,
     * в котором находится процесс обновления репозитория.
     *
     * @return статус состояния
     */
    fun getStatus(): LiveData<Status> {
        return status
    }

    /**
     * Этот метод возвращает все группы, упоминаемые в расписании.
     *
     * @return список групп
     */
    fun getGroups(): LiveData<Array<String>> {
        return db.lessonDao().getGroups()
    }

    /**
     * Этот метод позволяет получить имена всех преподавателей, упоминаемых в расписании.
     *
     * @return список учителей
     */
    fun getTeachers(): LiveData<Array<String>> {
        return db.lessonDao().getTeachers()
    }

    /**
     * Этот метод позволяет получить список всех предметов в расписании для заданной группы.
     * @param group группа
     * @return список предметов
     */
    fun getSubjects(group: String): LiveData<Array<String>> {
        return db.lessonDao().getSubjectsForGroup(group)
    }

    /**
     * Этот метод возращает список занятий в этот день у группы у данного преподавателя.
     * Если группа не указана, то возвращается список занятий у преподавателя в этот день.
     * Если учитель не указан, то возвращается список занятй у группы в этот день.
     *
     * @param date день
     * @param teacher преподаватель
     * @param group группа
     * @return список занятий
     */

    fun getLessons(group: String?, teacher: String?, date: Calendar): LiveData<Array<Lesson>>{
        return if (teacher != null && group != null) db.lessonDao().getLessonsForGroupWithTeacher(
            date,
            group,
            teacher
        ) else if (teacher != null) db.lessonDao()
            .getLessonsForTeacher(date, teacher) else if (group != null) db.lessonDao()
            .getLessonsForGroup(date, group) else MutableLiveData(
            arrayOf()
        )
    }

    /**
     * Этот метод предназначен для сохранения последней выбранной группы перед закрытием приложения.
     *
     * @param group группа для сохранения
     */
    fun saveGroup(group: String?) {
        preferences.edit()
            .putString("savedGroup", group)
            .apply()
    }

    /**
     * Этот метод возвращает сохраненную группу.
     *
     * @return группа
     */
    fun getSavedGroup(): String? {
        return preferences.getString("savedGroup", null)
    }

    /**
     * Этот метод используется для обновления БД приложения занятиями
     * @param linksGetter метод для получения ссылок на файлы расписания
     * @param parser парсер файлов расписания
     */
    private fun updateSchedule(linksGetter: Callable<List<String>>,
                               parser: (file: File) -> List<Lesson>) {
        var scheduleLinks: List<String> = java.util.ArrayList()
        try {
            scheduleLinks = linksGetter.call()
        } catch (ignored: Exception) { /*Not required*/ }
        if (scheduleLinks.isEmpty())
            status.postValue(Status(
                context.getString(R.string.schedule_download_error),
                0))
        for (link in scheduleLinks) {
            status.postValue(Status(
                context.getString(R.string.schedule_download_status),
                10))
            api.getScheduleFile(link).enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    if (response.body() != null) {
                        status.postValue(
                            Status(context.getString(R.string.schedule_parsing_status), 33))
                        /*try {
                            /*PDDocument.load(response.body()!!.byteStream()).use { pdfFile ->
                                val lessons: List<Lesson> = parser.invoke(pdfFile)
                                db.lessonDao().insertMany(lessons)*/
                                status.postValue(
                                    Status(context.getString(R.string.processing_completed_status), 100))
                            }
                        } catch (e: Exception) {
                            status.postValue(
                                Status(context.getString(R.string.schedule_parsing_error), 0))
                        }*/
                        response.body()?.close()
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    status.postValue(
                        Status(context.getString(R.string.schedule_download_error), 0))
                }
            })
        }
    }
}