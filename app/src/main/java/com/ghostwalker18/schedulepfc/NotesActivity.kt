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

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.ghostwalker18.schedulepfc.databinding.ActivityNotesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Этот классс представляет собой экран приложения, на котором отображаются заметки к занятиям.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
@AndroidEntryPoint
class NotesActivity : AppCompatActivity() {

    private var selectedNotes: MutableMap<Int, Note> = ConcurrentHashMap()
    @Inject lateinit var repository: NotesRepository
    private val listener = object : NoteAdapter.OnNoteClickListener {
        override fun onNoteSelected(note: Note, position: Int) {
            selectedNotes[position] = note
            binding.selectionPanel.visibility = View.VISIBLE
            binding.searchBar.visibility = View.GONE
            binding.selectedCount.text = selectedNotes.size.toString()
            decideMenuOptions()
        }

        override fun onNoteUnselected(note: Note, position: Int) {
            selectedNotes.remove(position, note)
            if (selectedNotes.isEmpty()) {
                binding.selectionPanel.visibility = View.GONE
                binding.searchBar.visibility = View.VISIBLE
            }
            binding.selectedCount.text = selectedNotes.size.toString()
            decideMenuOptions()
        }
    }
    private var group: String? = null
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var isEditAvailable = false
    private var isDeleteAvailable = false
    private var isShareAvailable = false
    private var isEditChanged = true
    private var isDeleteChanged = true
    private var isShareChanged = true
    private lateinit var binding : ActivityNotesBinding
    private lateinit var model: NotesModel
    private lateinit var filter: NotesFilterFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        model = ViewModelProvider(this)[NotesModel::class.java]

        intent.extras?.run {
            group = getString("group")
            startDate = DateConverters().fromString(getString("date"))
            endDate = startDate
            if(savedInstanceState == null){
                model.run{
                    setGroup(group)
                    setStartDate(startDate)
                    setEndDate(endDate)
                }
            }
        }
        model.getNotes().observe(this) {
                notes -> run {
            if (notes?.size == 0)
                binding.noNotesNotification.visibility = View.VISIBLE
            else
                binding.noNotesNotification.visibility = View.GONE
            binding.notes.adapter = NoteAdapter(notes!!, listener)
        }
        }
        filter = NotesFilterFragment()
        filter.listener = object: NotesFilterFragment.VisibilityListener{
            override fun onFragmentShow() {
                val adapter = binding.notes.adapter as NoteAdapter
                adapter.isClickable = false
            }

            override fun onFragmentHide() {
                val adapter = binding.notes.adapter as NoteAdapter
                adapter.isClickable = true
            }

        }
        binding.filter.setOnClickListener{ openFilterFragment() }
        binding.editNote.setOnClickListener{ openAddNote() }
        binding.search.apply {
            addTextChangedListener {
                doAfterTextChanged {
                    var keyword: String? = it.toString().trim { it <= ' ' }
                    if (keyword == "") keyword = null
                    model.setKeyword(keyword)
                }
            }
        }

        binding.selectionCancel.setOnClickListener{ resetSelection() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_notes_activity, menu)
        val editItemView = menu?.findItem(R.id.action_edit)?.actionView as ImageView
        editItemView.apply {
            setPadding(20,10,20,10)
            setImageResource(R.drawable.baseline_edit_document_36)
            setOnClickListener{ openEditNote() }
        }
        val deleteItemView = menu.findItem(R.id.action_delete)?.actionView as ImageView
        deleteItemView.apply {
            setPadding(20,10,20,10)
            setImageResource(R.drawable.baseline_delete_36)
            setOnClickListener{ deleteNotes() }
        }
        val shareItemView = menu.findItem(R.id.action_share)?.actionView as ImageView
        shareItemView.apply {
            setPadding(20,10,20,10)
            setImageResource(R.drawable.baseline_share_36)
            setOnClickListener{ shareNotes() }
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (isEditChanged) {
            toggleMenuItem(menu, R.id.action_edit, isEditAvailable)
            isEditChanged = false
        }
        if (isDeleteChanged) {
            toggleMenuItem(menu, R.id.action_delete, isDeleteAvailable)
            isDeleteChanged = false
        }
        if (isShareChanged) {
            toggleMenuItem(menu, R.id.action_share, isShareAvailable)
            isShareChanged = false
        }
        return true
    }

    /**
     * Этот метод отвечает за появление/скрытие элемента меню.
     */
    private fun toggleMenuItem(menu: Menu, menuItemID: Int, isAvailable: Boolean) {
        val open = AnimatorInflater
            .loadAnimator(this, R.animator.menu_item_appear) as AnimatorSet
        val close = AnimatorInflater
            .loadAnimator(this, R.animator.menu_item_disappear) as AnimatorSet
        val menuItem = menu.findItem(menuItemID)
        if (isAvailable) {
            open.setTarget(menuItem.actionView)
            open.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    menuItem.setVisible(true)
                }
            })
            open.start()
        } else {
            close.setTarget(menuItem.actionView)
            close.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    menuItem.setVisible(false)
                }
            })
            close.start()
        }
    }

    /**
     * Этот метод сбрасывает выделение всех заметок.
     */
    private fun resetSelection() {
        for (position in selectedNotes.keys) {
            val item = binding.notes
                .findViewHolderForAdapterPosition(position) as NoteAdapter.ViewHolder
            item.isSelected  = false
            listener.onNoteUnselected(selectedNotes[position]!!, position)
        }
    }

    /**
     * Этот метод открывает активность для редактирования или добавления заметки.
     */
    private fun openAddNote() {
        val intent = Intent(this, EditNoteActivity::class.java)
        val bundle = Bundle()
        bundle.putString("group", group)
        if (startDate != null) {
            bundle.putString("date", DateConverters().toString(startDate))
        }
        intent.putExtras(bundle)
        startActivity(intent)
    }

    /**
     * Этот метод окрывает панель фильтра.
     */
    private fun openFilterFragment() {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(R.id.notes_container, filter)
            .commit()
    }

    /**
     * Этот метод позволяет поделиться выбранными заметками.
     * @return
     */
    private fun shareNotes() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        var notes = ""
        for (note in selectedNotes.values) {
            notes += note.toString() + "\n"
        }
        intent.putExtra(Intent.EXTRA_TEXT, notes)
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
        resetSelection()
        decideMenuOptions()
    }

    /**
     * Этот метод позволяет удалить выбранные заметки.
     * @return
     */
    private fun deleteNotes() {
        repository.deleteNotes(selectedNotes.values)
        resetSelection()
        decideMenuOptions()
    }

    /**
     * Этот метод позволяет, если выбранна одна заметка,
     * открыть экран приложения для ее редактирования.
     * @return
     */
    private fun openEditNote() {
        val intent = Intent(this, EditNoteActivity::class.java)
        intent.putExtra("noteID", selectedNotes.entries.iterator().next().value.id)
        startActivity(intent)
        resetSelection()
        decideMenuOptions()
    }

    /**
     * Этот метод позволяет определить, какие опции должны быть в меню.
     */
    private fun decideMenuOptions() {
        isEditChanged = isEditAvailable != (selectedNotes.size == 1)
        isShareChanged = isShareAvailable != (selectedNotes.isNotEmpty())
        isDeleteChanged = isDeleteAvailable != (selectedNotes.isNotEmpty())
        isEditAvailable = selectedNotes.size == 1
        isShareAvailable = selectedNotes.isNotEmpty()
        isDeleteAvailable = selectedNotes.isNotEmpty()
        invalidateMenu()
    }
}