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
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.ghostwalker18.schedulepfc.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Этот класс представляет собой основной экран приложения.
 *
 * @author  Ипатов Никита
 * @since 1.0
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var daysFragment: DaysFragment
    @Inject lateinit var repository: ScheduleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        daysFragment = binding.daysFragment.getFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_share -> return shareSchedule()
            R.id.action_download -> return downloadScheduleFile()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Этот метод используется для того, чтобы поделиться расписанием из открытых элементов
     * в доступных приложениях.
     */
    private fun shareSchedule(): Boolean {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        var schedule = ""
        for (day in daysFragment.getDays()) {
            if (day.isOpened) {
                schedule += day.getSchedule()
            }
        }
        if (schedule == "") {
            Toast.makeText(this, R.string.nothing_to_share, Toast.LENGTH_SHORT).show()
            return true
        }
        intent.putExtra(Intent.EXTRA_TEXT, schedule)
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
        return true
    }

    /**
     * Этот метод используется для скачивания файлов расписания и помещения их в
     * папку загрузок.
     */
    private fun downloadScheduleFile(): Boolean {
        Thread {
            val links: MutableList<String> = ArrayList()
            val downloadFor = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("downloadFor", "all")
            /*if (downloadFor.equals("all") || downloadFor.equals("first")){
                val linksForFirstCorpusSchedule = repository.getLinksForFirstCorpusSchedule()
                links.addAll(linksForFirstCorpusSchedule)
            }
            if (downloadFor.equals("all") || downloadFor.equals("second")){
                val linksForSecondCorpusSchedule = repository.getLinksForSecondCorpusSchedule()
                links.addAll(linksForSecondCorpusSchedule)
            }
            if (downloadFor.equals("all") || downloadFor.equals("third")){
                val linksForThirdCorpusSchedule = repository.getLinksForThirdCorpusSchedule()
                links.addAll(linksForThirdCorpusSchedule)
            }*/
            val downloadDialog = DownloadDialog()
            val args = Bundle()
            args.putInt("number_of_files", links.size)
            args.putStringArray("links", links.toTypedArray<String>())
            val mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            args.putString("mime_type", mimeType)
            val downloadTitle = getString(R.string.days_tab)
            args.putString("download_title", downloadTitle)
            downloadDialog.arguments = args
            downloadDialog.show(supportFragmentManager, "download")
        }.start()
        return true
    }
}