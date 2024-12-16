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

import android.Manifest.permission
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ghostwalker18.schedulepfc.databinding.FragmentNoteBinding
import kotlin.properties.Delegates

/**
 * Этот класс служит для отображения списка заметок.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
class NoteAdapter(notes: Array<Note>, listener: OnNoteClickListener) : RecyclerView.Adapter<NoteAdapter.ViewHolder>(){
    interface OnNoteClickListener {
        fun onNoteSelected(note: Note, position: Int)
        fun onNoteUnselected(note: Note, position: Int)
    }

    private val notes: Array<Note>
    private val listener: OnNoteClickListener
    private lateinit var context: android.content.Context
    private var canAccessPhoto by Delegates.notNull<Boolean>()
    var isClickable: Boolean = true

    init {
        this.notes = notes
        this.listener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.binding.date.text = DateConverters().toString(note.date)
        holder.binding.theme.text = note.theme
        holder.binding.text.text = note.text
        canAccessPhoto = checkPhotoAccess()
        if (canAccessPhoto) {
            try {
                holder.binding.image.setImageURI(Uri.parse(note.photoID))
            } catch (e: Exception) {
                holder.binding.error.text = context.getString(R.string.photo_error)
            }
        }
        if (note.photoID != null && !canAccessPhoto) holder.binding.error.setText(R.string.gallery_access_denied)
        holder.binding.note.setOnClickListener{
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

    private fun checkPhotoAccess(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            (
                    ContextCompat.checkSelfPermission(context, permission.READ_MEDIA_IMAGES) ==
                            PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(context, permission.READ_MEDIA_VIDEO) ==
                            PackageManager.PERMISSION_GRANTED
                    )
        ) {
            true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(context, permission.READ_MEDIA_VISUAL_USER_SELECTED) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else ContextCompat.checkSelfPermission(context, permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Этот класс служит для работы с элементом списка.
     *
     * @author Ипатов Никита
     * @since 1.0
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding: FragmentNoteBinding
        var isSelected = false
            set(value) {
                field = value
                binding.checked.visibility = if (isSelected) View.VISIBLE else View.GONE
            }
        init {
            binding = FragmentNoteBinding.bind(itemView)
        }

    }
}