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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.ghostwalker18.schedulepfc.databinding.FragmentDaysBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.function.Consumer
import javax.inject.Inject

/**
 * Этот класс представляет собой элемент интерфейса, используемый для
 * отображения расписания занятий.
 *
 * @author  Ипатов Никита
 * @since 1.0
 */
@AndroidEntryPoint
class DaysFragment: Fragment(),
    OnSharedPreferenceChangeListener {

    private var _binding: FragmentDaysBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var repository: ScheduleRepository
    private val days: MutableList<ScheduleItemFragment> = ArrayList()
    private lateinit var state: ScheduleState
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.registerOnSharedPreferenceChangeListener(this)
        state = ViewModelProvider(requireActivity())[ScheduleState::class.java]
        if (savedInstanceState == null) {
            days.add(ScheduleItemFragment.newInstance(R.string.monday))
            days.add(ScheduleItemFragment.newInstance(R.string.tuesday))
            days.add(ScheduleItemFragment.newInstance(R.string.wednesday))
            days.add(ScheduleItemFragment.newInstance(R.string.thursday))
            days.add(ScheduleItemFragment.newInstance(R.string.friday))
        } else {
            childFragmentManager
                .fragments
                .forEach(Consumer { item: Fragment ->
                    days.add(item as ScheduleItemFragment)
                })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDaysBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpGroupSearch()
        setUpTeacherSearch()
        enableTeacherSearch()
        binding.forwardButton.setOnClickListener{ state.goNextWeek() }
        binding.backButton.setOnClickListener{ state.goPreviousWeek() }
        repository.getStatus().observe(viewLifecycleOwner) { (text, progress) ->
            binding.updateScheduleProgress.progress = progress
            binding.updateScheduleStatus.text = text
        }
        if (savedInstanceState == null) {
            for (day in days) {
                childFragmentManager
                    .beginTransaction()
                    .add(R.id.days_container, day)
                    .commit()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        repository.saveGroup(state.getGroup().value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        when (s) {
            "addTeacherSearch" -> enableTeacherSearch()
        }
    }

    /**
     * Этот метод используется для получения доступа к элементам расписания.
     * @return список отдельных элементов расписания
     */
    fun getDays(): List<ScheduleItemFragment> {
        return days
    }

    /**
     * Этот метод используется для настройки элемента поиска по группе.
     */
    private fun setUpGroupSearch(){
        binding.group.apply {
            setOnItemClickListener{ adapterView, view, i, _ ->
                run {
                    val group = adapterView.getItemAtPosition(i).toString()
                    state.setGroup(group)
                    val input = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    input.hideSoftInputFromWindow(view.applicationWindowToken, 0)
                }
            }
            addTextChangedListener {
                doAfterTextChanged { if (it.toString() == "") state.setGroup(null) }
            }
        }
        val savedGroup = repository.getSavedGroup()
        binding.group.setText(savedGroup)
        state.setGroup(savedGroup)
        binding.groupClear.setOnClickListener {
            state.setGroup(null)
            binding.group.setText("")
        }
        repository.getGroups().observe(viewLifecycleOwner) { strings ->
            val adapter = ArrayAdapter(requireContext(), R.layout.autocomplete_item_layout, strings)
            binding.group.setAdapter(adapter)
            state.setGroup(binding.group.text.toString())
        }
    }

    /**
     * Этот метод используется для настройки элемента поиска по преподавателю.
     */
    private fun setUpTeacherSearch(){
        binding.teacher.apply {
            setOnItemClickListener { adapterView, view, i, _ ->
                run {
                    val teacher = adapterView.getItemAtPosition(i).toString()
                    state.setTeacher(teacher)
                    val input = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    input.hideSoftInputFromWindow(view.applicationWindowToken, 0)
                }
                addTextChangedListener {
                    doAfterTextChanged { if (it.toString() == "") state.setTeacher(null) }
                }
            }
        }
        binding.clearTeacher.setOnClickListener {
            state.setTeacher(null)
            binding.teacher.setText("")
        }
        repository.getTeachers().observe(viewLifecycleOwner) {
            val adapter = ArrayAdapter(requireContext(), R.layout.autocomplete_item_layout, it)
            binding.teacher.setAdapter(adapter)
            state.setTeacher(binding.teacher.text.toString())
        }
    }

    /**
     * Этот метод используется для отображения элемента поиска по преподавателю
     * согласно настройкам приложения.
     */
    private fun enableTeacherSearch(){
        val addTeacherSearch = prefs.getBoolean("addTeacherSearch", false)
        binding.teacherLabel.visibility = if (addTeacherSearch) View.VISIBLE else View.GONE
        binding.clearTeacher.visibility = if (addTeacherSearch) View.VISIBLE else View.GONE
        binding.teacher.visibility = if (addTeacherSearch) View.VISIBLE else View.GONE
        if(!addTeacherSearch){
            state.setTeacher(null)
            binding.teacher.setText("")
        }
    }
}