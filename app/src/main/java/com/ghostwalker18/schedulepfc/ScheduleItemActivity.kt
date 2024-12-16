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
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TableLayout
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.ghostwalker18.schedulepfc.databinding.ActivityScheduleItemBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

/**
 * Этот класс представляет собой экран приложения для отображения расписания на день.
 *
 * @author  Ипатов Никита
 * @since 1.0
 */
@AndroidEntryPoint
class ScheduleItemActivity : AppCompatActivity() {

    private lateinit var lessons: LiveData<Array<Lesson>>
    private lateinit var binding: ActivityScheduleItemBinding
    @Inject lateinit var repository: ScheduleRepository
    private var teacher: String? = null
    private var group: String? = null
    private var date: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var bundle = intent.extras
        if (bundle == null) {
            bundle = savedInstanceState!!
        }
        teacher = bundle.getString("teacher")
        group = bundle.getString("group")
        date = DateConverters().fromString(bundle.getString("date"))
        date?.set(Calendar.DAY_OF_WEEK, bundle.getInt("dayOfWeek"))
        binding.toolbar.title = generateTitle(date!!)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        lessons = repository.getLessons(group, teacher, date!!)
        lessons.observe(this){ lessons -> populateTable(binding.schedule, lessons) }
        binding.notes.setOnClickListener { openNotesActivity() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_schedule_item_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_share) shareSchedule()
        else super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("teacher", teacher)
        outState.putString("group", group)
        outState.putString("date", DateConverters().toString(date))
        super.onSaveInstanceState(outState)
    }

    /**
     * Этот метод используется для создания заголовка экрана
     * @param date дата
     * @return заголовок в строковом формате
     */
    private fun generateTitle(date: Calendar): String{
        var title = getString(R.string.day_table)
        title = title + " " + Utils.generateDateForTitle(date)
        if (Utils.isDateToday(date)) {
            title = title + " - " + resources.getString(R.string.today)
        }
        return title
    }

    /**
     * Этот метод используется для наполнения таблицы расписания данными.
     * @param table таблица для заполнения
     * @param lessons данные для заполнения
     */
    private fun populateTable(table: TableLayout, lessons: Array<Lesson>) {
        var tableRowLayout = R.layout.schedule_row
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            tableRowLayout = R.layout.schedule_row_with_times
        var counter = 0
        for (lesson in lessons) {
            counter++
            val tr = addLesson(table, tableRowLayout, lesson)
            if (counter % 2 == 1) tr.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_500))
        }
    }

    private fun addLesson(table: TableLayout ,tableRowLayout: Int, lesson: Lesson): TableRow {
        return TableRow(this)
    }

    /**
     * Этот метод используется чтобы поделиться расписанием на этот день.
     * @return
     */
    private fun shareSchedule(): Boolean {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        var schedule = getString(R.string.date) + ": "
        schedule = schedule + DateConverters().toString(date) + "\n"
        schedule += "\n"
        for (lesson in lessons.value!!) {
            schedule += lesson.toString()
            schedule += "\n"
        }
        schedule += "\n"
        intent.putExtra(Intent.EXTRA_TEXT, schedule)
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
        return true
    }

    /**
     * Этот метод используется чтобы открыть экран с заметками для этого дня.
     */
    private fun openNotesActivity() {
        val bundle = Bundle()
        val intent = Intent(this, NotesActivity::class.java)
        bundle.putString("group", group)
        bundle.putString("date", DateConverters().toString(date))
        intent.putExtras(bundle)
        startActivity(intent)
    }
}