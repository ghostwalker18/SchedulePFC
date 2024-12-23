package com.ghostwalker18.schedulepfc

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ghostwalker18.schedulepfc.databinding.FragmentPreviewBinding

class PreviewFragment : Fragment(){
    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!

    /**
     * Этот интерфейс задает слушателя события удаления изображения из галереи превью.
     */
    private var listener: ((Uri) -> Unit)? = null
    private var isEditable = false
    private var currentItem = 0
    private var photoUris: ArrayList<Uri>? = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentPreviewBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.preview.setOnTouchListener(object : OnSwipeListener(requireContext()) {
            override fun onSwipeTop() {
                deletePhoto()
            }

            override fun onSwipeLeft() {
                showNextPhoto()
            }

            override fun onSwipeRight() {
                showPreviousPhoto()
            }
        })
        binding.preview.setOnClickListener {
            if (photoUris?.isNotEmpty() == true) {
                val intent = Intent(
                    requireActivity(),
                    PhotoViewActivity::class.java
                )
                intent.putExtra("photo_uri", photoUris!![currentItem].toString())
                startActivity(intent)
            }
        }
        binding.delete.visibility = if (isEditable) View.VISIBLE else View.GONE
        binding.delete.setOnClickListener{ deletePhoto() }
        binding.previous.setOnClickListener{ showPreviousPhoto() }
        binding.next.setOnClickListener{ showNextPhoto() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Этот метод используется для задания списка URI отображаемых фотографий.
     * @param uris отображаемые фотографии
     */
    fun setImageIDs(uris: ArrayList<Uri>?) {
        photoUris = uris
        if (photoUris?.isNotEmpty() == true)
            binding.preview.setImageURI(photoUris!![photoUris!!.size - 1])
        binding.delete.visibility = if(photoUris?.isNotEmpty() == true) View.VISIBLE else View.GONE
    }

    /**
     * Этот метод задает слушателя события удаления фото.
     */
    fun setListener(listener: (Uri) -> Unit) {
        this.listener = listener
    }

    /**
     * Этот метод задает возможность удаления фотографий из списка.
     * @param editable возможность удаления
     */
    fun setEditable(editable: Boolean) {
        isEditable = editable
    }

    /**
     * Этот метод используется для отображения следущего фото.
     */
    private fun showNextPhoto() {
        if (photoUris?.isNotEmpty() == true) {
            currentItem++
            if (currentItem >= photoUris!!.size)
                currentItem = 0
            binding.preview.setImageURI(photoUris!![currentItem])
        }
    }

    /**
     * Этот метод используется для отображения предыдущего фото
     */
    private fun showPreviousPhoto() {
        if (photoUris?.isNotEmpty() == true) {
            currentItem--
            if (currentItem < 0) currentItem = photoUris!!.size - 1
            binding.preview.setImageURI(photoUris!![currentItem])
        }
    }

    /**
     * Этот метод используется для удаления текущей фотографии из списка
     */
    private fun deletePhoto() {
        if (isEditable && photoUris?.isNotEmpty() == true) {
            val deletedUri = photoUris!!.removeAt(currentItem)
            if (photoUris!!.isEmpty()) {
                binding.preview.setImageResource(R.drawable.baseline_no_photography_72)
                binding.delete.visibility = View.GONE
                return
            }
            currentItem--
            if (currentItem < 0)
                currentItem = photoUris!!.size - 1
            if (currentItem < photoUris!!.size)
                binding.preview.setImageURI(photoUris!![currentItem])
            listener?.invoke(deletedUri)
        }
    }
}