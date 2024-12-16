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
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostwalker18.schedulepfc.databinding.ActivityShareAppBinding

/**
 * Этот класс представляет собой экран, где пользователь может поделиться ссылкой на приложение.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
class ShareAppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShareAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.shareLink.setOnClickListener { shareLink() }
    }

    /**
     * Этот метод используется, чтобы поделиться ссылокой на расписание в RuStore.
     */
    private fun shareLink() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_TEXT, resources.getText(R.string.rustore_link))
        startActivity(shareIntent)
    }
}