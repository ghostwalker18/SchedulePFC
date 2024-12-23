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

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.ghostwalker18.schedulepfc.databinding.FragmentFilterBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

/**
 * Этот класс служит для отображения панели фильтров заметок.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
@AndroidEntryPoint
class NotesFilterFragment : Fragment() {

    interface VisibilityListener{
        fun onFragmentShow()
        fun onFragmentHide()
    }

    @Inject lateinit var repository: ScheduleRepository
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    private val model: NotesModel by viewModels()
    var listener: VisibilityListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setStartDate.setOnClickListener { setStartDate() }
        binding.setEndDate.setOnClickListener { setEndDate() }
        binding.close.setOnClickListener { close() }
        binding.groupClear.setOnClickListener {
            binding.group.setText("")
            model.setGroup(null)
        }

        binding.group.setText(model.getGroup())
        repository.getGroups().observe(viewLifecycleOwner){
            val adapter = ArrayAdapter(requireContext(), R.layout.autocomplete_item_layout, it)
            binding.group.setAdapter(adapter)
        }
        binding.group.setOnItemClickListener { adapterView, view, i, _ ->
            model.setGroup(adapterView.getItemAtPosition(i).toString())
            val input = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            input.hideSoftInputFromWindow(view.applicationWindowToken, 0)
        }

        model.getStartDate().observe(viewLifecycleOwner){
            binding.startDate.text = DateConverters().toString(it)
        }

        model.getEndDate().observe(viewLifecycleOwner){
            binding.endDate.text = DateConverters().toString(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener?.onFragmentShow()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Этот метод служит для сокрытия фргмента с экрана.
     */
    private fun close() {
        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .remove(this)
            .commit()
        listener?.onFragmentHide()
    }

    /**
     * Этот метод открывает ввод для задания начальной даты выдачи заметок.
     */
    private fun setStartDate() {
        val datePickerFragment = DatePickerFragment("start")
        datePickerFragment.show(childFragmentManager, "1")
    }

    /**
     * Этот метод открывает ввод для задания конечной даты вывода заметок.
     */
    private fun setEndDate() {
        val datePickerFragment = DatePickerFragment("end")
        datePickerFragment.show(childFragmentManager, "2")
    }

    /**
     * Этот класс служит для задания начальной/конечной даты выдачи заметок.
     */
    class DatePickerFragment(dateType: String): DialogFragment(), OnDateSetListener {
        private val dateType: String
        private lateinit var model: NotesModel

        init {
            this.dateType = dateType
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            model = ViewModelProvider(requireActivity())[NotesModel::class.java]
            // Use the current date as the default date in the picker.
            // Use the current date as the default date in the picker.
            val c = Calendar.getInstance()
            val year = c[Calendar.YEAR]
            val month = c[Calendar.MONTH]
            val day = c[Calendar.DAY_OF_MONTH]
            // Create a new instance of DatePickerDialog and return it.
            return DatePickerDialog(requireContext(), this, year, month, day)
        }

        override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
            val c = Calendar.getInstance()
            c[year, month] = day

            when (dateType) {
                "start" -> model.setStartDate(c)
                "end" -> model.setEndDate(c)
            }
        }
    }
}