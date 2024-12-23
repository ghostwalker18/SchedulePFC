package com.ghostwalker18.schedulepfc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostwalker18.schedulepfc.databinding.ActivityPhotoViewBinding

class PhotoViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoViewBinding
    private lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var bundle = intent.extras
        if (bundle == null) {
            bundle = savedInstanceState!!
        }
        photoUri = Uri.parse(bundle.getString("photo_uri"))
        binding.photo.setImageURI(photoUri)
        binding.backButton.setOnClickListener { finishAfterTransition() }
        binding.shareButton.setOnClickListener { sharePhoto() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("photo_uri", photoUri.toString())
        super.onSaveInstanceState(outState)
    }

    /**
     * Этот метод используетсяя чтобы поделиться отображаемым фото.
     */
    private fun sharePhoto() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("image/*")
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri)
        startActivity(Intent.createChooser(shareIntent, null))
    }
}