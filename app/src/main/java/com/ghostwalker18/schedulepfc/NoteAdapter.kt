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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Этот класс служит для отображения списка заметок.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
class NoteAdapter(private val notes: Array<Note>, private val listener: OnNoteClickListener) :
    RecyclerView.Adapter<NoteViewHolder>(){
    interface OnNoteClickListener {
        fun onNoteSelected(note: Note, position: Int)
        fun onNoteUnselected(note: Note, position: Int)
    }

    var isClickable: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.fragment_note, parent, false)
        return NoteViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.setNote(note)
        holder.itemView.setOnClickListener{
            if (!isClickable){
                return@setOnClickListener
            }
            holder.isSelected = !holder.isSelected
            if (holder.isSelected) {
                listener.onNoteSelected(note, position)
            } else {
                listener.onNoteUnselected(note, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }
}