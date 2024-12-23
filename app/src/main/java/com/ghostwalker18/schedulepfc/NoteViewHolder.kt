package com.ghostwalker18.schedulepfc

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ghostwalker18.schedulepfc.databinding.FragmentNoteBinding
import com.ghostwalker18.schedulepfc.databinding.FragmentPreviewBinding

class NoteViewHolder(itemView: View, val context: Context) : RecyclerView.ViewHolder(itemView){
    private var currentItem = 0
    private var photoUris: ArrayList<Uri>? = null
    private lateinit var binding: FragmentNoteBinding
    var isSelected = false
        set(value) {
            field = value
            binding.checked.visibility = if (isSelected) View.VISIBLE else View.GONE
        }

    /**
     * Этот метод используется для задания заметки для отображения.
     * @param note отображаемая заметка
     */
    fun setNote(note: Note) {
        binding.date.text = DateConverters().toString(note.date)
        binding.theme.text = note.theme
        binding.text.text = note.text
        photoUris = note.photoIDs
        binding.notePhotos.previous.visibility = if (photoUris != null && photoUris!!.size > 1)  View.VISIBLE else View.INVISIBLE
        binding.notePhotos.next.visibility = if (photoUris != null && photoUris!!.size > 1)  View.VISIBLE else View.INVISIBLE
        if (checkPhotoAccess()) {
            try {
                if (photoUris != null && photoUris!!.size > 0) binding.notePhotos.preview.setImageURI(
                    photoUris!![photoUris!!.size - 1]
                )
            } catch (e: Exception) {
                binding.error.text = context.getString(R.string.photo_error)
            }
        }
        if (note.photoIDs != null && !checkPhotoAccess()) binding.error.setText(R.string.gallery_access_denied)
    }

    /**
     * Этот метод используется для отображения следущего фото.
     */
    private fun showNextPhoto() {
        if (photoUris!!.isNotEmpty()) {
            currentItem++
            if (currentItem >= photoUris!!.size) currentItem = 0
            try {
                binding.notePhotos.preview.setImageURI(photoUris!![currentItem])
            } catch (e: java.lang.Exception) {
                binding.error.text = context.getString(R.string.photo_error)
            }
        }
    }

    /**
     * Этот метод используется для отображения предыдущего фото
     */
    private fun showPreviousPhoto() {
        if (photoUris!!.size != 0) {
            currentItem--
            if (currentItem < 0) currentItem = photoUris!!.size - 1
            try {
                binding.notePhotos.preview.setImageURI(photoUris!![currentItem])
            } catch (e: java.lang.Exception) {
                binding.error.text = context.getString(R.string.photo_error)
            }
        }
    }
    private fun checkPhotoAccess(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
                    PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) ==
                    PackageManager.PERMISSION_GRANTED)) {
            true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }
}