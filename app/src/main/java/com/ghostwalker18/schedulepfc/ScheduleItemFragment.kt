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

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.ghostwalker18.schedulepfc.databinding.FragmentScheduleItemBinding
import com.ghostwalker18.schedulepfc.databinding.ScheduleRowBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

/**
 * Этот класс предсавляет собой кастомный элемент GUI,
 * используемый для отображения расписания на день.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
@AndroidEntryPoint
class ScheduleItemFragment : Fragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var _binding: FragmentScheduleItemBinding? = null
    private val binding get() = _binding!!
    private val date = MutableLiveData<Calendar>()
    private var lessons: LiveData<Array<Lesson>> = MutableLiveData()
    private var dayOfWeekID: Int = 0
    private var _isOpened = false
    val isOpened: Boolean
        get() {return _isOpened}
    private lateinit var state: ScheduleState
    @Inject lateinit var repository: ScheduleRepository
    @Inject lateinit var preferences: SharedPreferences
    private lateinit var mode: String

    companion object {
        private val weekdaysNumbers = HashMap<Int, Int>()
        init {
            weekdaysNumbers[R.string.monday] = Calendar.MONDAY
            weekdaysNumbers[R.string.tuesday] = Calendar.TUESDAY
            weekdaysNumbers[R.string.wednesday] = Calendar.WEDNESDAY
            weekdaysNumbers[R.string.thursday] = Calendar.THURSDAY
            weekdaysNumbers[R.string.friday] = Calendar.FRIDAY
        }

        fun newInstance(dayOfWeekId: Int): ScheduleItemFragment {
            val args = Bundle()
            args.putInt("dayOfWeek", dayOfWeekId)
            val fragment = ScheduleItemFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = ViewModelProvider(requireActivity())[ScheduleState::class.java]
        dayOfWeekID = requireArguments().getInt("dayOfWeek")
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        state.getCalendar().observe(viewLifecycleOwner) {
            date.setValue(
                Calendar.Builder()
                    .setWeekDate(state.getYear(), state.getWeek(), weekdaysNumbers[dayOfWeekID]!!)
                    .build()
            )
        }
        state.getGroup().observe(viewLifecycleOwner) {
            lessons = repository.getLessons(
                state.getGroup().value,
                state.getTeacher().value,
                date.value!!
            )
            lessons.observe(viewLifecycleOwner){ populateTable(binding.schedule, it) }
        }

        date.observe(viewLifecycleOwner) { date: Calendar ->
            _isOpened = Utils.isDateToday(date)
            binding.button.text = generateTitle(date, dayOfWeekID)
            lessons = repository.getLessons(
                state.getGroup().value,
                state.getTeacher().value,
                date
            )
            lessons.observe(
                viewLifecycleOwner){ lessons: Array<Lesson> ->
                populateTable(binding.schedule, lessons)
            }
            showTable()
        }
        setUpMode()
        binding.notes.setOnClickListener { openNotesActivity() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        when (s) {
            "scheduleStyle" -> {
                if (_isOpened) toggleSchedule()
                setUpMode()
            }
        }
    }

    /**
     * Этот метод позвоволяет получить расписание для этого элемента в виде
     * форматированной строки.
     *
     * @return расписание на этот день
     */
    fun getSchedule(): String {
        var schedule = getString(R.string.date) + ": "
        schedule = schedule + DateConverters().toString(date.value) + "\n"
        schedule += "\n"
        for (lesson in lessons.value!!) {
            schedule += lesson.toString()
            schedule += "\n"
        }
        schedule += "\n"
        return schedule
    }

    /**
     * Этот метод устанавливает режим отображения расписания:
     * в отдельном окне или в таблице в элементе.
     */
    private fun setUpMode() {
        mode = preferences.getString("scheduleStyle", "in_fragment")!!
        when (mode) {
            "in_fragment" -> {
                binding.button.setOnClickListener{ toggleSchedule() }
                binding.button.setCompoundDrawablesWithIntrinsicBounds(
                    null, null,
                    ResourcesCompat.getDrawable(resources,
                        R.drawable.baseline_keyboard_arrow_down_24, null), null
                )
            }

            "in_activity" -> {
                binding.button.setOnClickListener{ openScheduleInActivity() }
                binding.button.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    null, null)
            }
        }
    }

    /**
     * Этот метод используется для переключения состояния таблицы между скрыта/видима.
     */
    private fun toggleSchedule() {
        _isOpened = !_isOpened
        showTable()
    }

    /**
     * Этот метод используется для настройки визуального отображения таблицы расписания
     */
    private fun showTable() {
        binding.schedule.visibility = if (_isOpened && mode == "in_fragment") View.VISIBLE else View.GONE
        binding.notes.visibility = if (_isOpened && mode == "in_fragment") View.VISIBLE else View.GONE
        binding.button.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ResourcesCompat.getDrawable(resources,
                if (_isOpened && mode == "in_fragment")
                    R.drawable.baseline_keyboard_arrow_up_24
                else R.drawable.baseline_keyboard_arrow_down_24, null),
            null
        )
        val open = AnimatorInflater
            .loadAnimator(requireContext(), R.animator.drop_down) as AnimatorSet
        open.setTarget(binding.schedule)
        open.start()
    }

    /**
     * Этот метод открывает отдельное окно для отображения расписания.
     */
    private fun openScheduleInActivity() {
        val bundle = Bundle()
        bundle.putString("group", state.getGroup().value)
        bundle.putString("teacher", state.getTeacher().value)
        bundle.putString("date", DateConverters().toString(state.getCalendar().value))
        bundle.putInt("dayOfWeek", weekdaysNumbers[dayOfWeekID]!!)
        val intent = Intent(this.activity, ScheduleItemActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    /**
     * Этот метод окрывает экран с заметками для этого дня.
     */
    private fun openNotesActivity() {
        val bundle = Bundle()
        val intent = Intent(this.activity, NotesActivity::class.java)
        bundle.putString("group", state.getGroup().value)
        bundle.putString("date", DateConverters().toString(date.value))
        intent.putExtras(bundle)
        startActivity(intent)
    }

    /**
     * Этот метод генерирует заголовок для этого элемента
     * @param date дата расписания
     * @param dayOfWeekId id строкового ресурса соответствующего дня недели
     * @return заголовок
     */
    private fun generateTitle(date: Calendar, dayOfWeekId: Int): String {
        val dayOfWeek = resources.getString(dayOfWeekId)
        var label = dayOfWeek + " (" + Utils.generateDateForTitle(date) + ")"
        if (Utils.isDateToday(date)) {
            label = label + " - " + resources.getString(R.string.today)
        }
        return label
    }

    /**
     * Этот метод используется для заполнения таблицы занятиями и задания ее стиля.
     * @param table таблица для заполнения
     * @param lessons занятия
     */
    private fun populateTable(table: TableLayout, lessons: Array<Lesson>) {
        table.removeViews(1, table.childCount - 1)
        var counter = 0
        for (lesson in lessons) {
            counter++
            val tr: TableRow = addLesson(table, lesson)
            if (counter % 2 == 1) tr.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray_500))
        }
    }

    /**
     * Этот метод используется для добавления занятия в таблицу
     * @param table таблица для добавления
     * @param lesson занятие
     * @return ряд таблицы, куда было добавлено занятие
     */
    private fun addLesson(table: TableLayout, lesson: Lesson): TableRow {
        val tr = ScheduleRowBinding.inflate(layoutInflater).apply {
            number.text = lesson.lessonNumber
            subject.text = lesson.subject
            teacher.text = lesson.teacher
            room.text = lesson.roomNumber
        }.root
        table.addView(tr, TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT))
        return tr
    }
}