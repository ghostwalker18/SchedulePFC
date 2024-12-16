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

import android.app.Dialog
import android.app.DownloadManager
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * Этот класс используется для скачивания файлов с подтверждением загрузки.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
class DownloadDialog : DialogFragment() {

    private lateinit var links: Array<String>
    private lateinit var mimeTypeOfFilesToDownload: String
    private lateinit var downloadTitle: String
    private val listener =
        DialogInterface.OnClickListener { _: DialogInterface?, which: Int ->
            if (which == Dialog.BUTTON_POSITIVE) {
                Thread {
                    val downloadManager = requireActivity().getSystemService(DownloadManager::class.java)
                    for (link in links) {
                        val request = DownloadManager.Request(Uri.parse(link))
                            .setMimeType(mimeTypeOfFilesToDownload)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setTitle(downloadTitle)
                            .setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                Utils.getNameFromLink(link)
                            )
                        downloadManager.enqueue(request)
                    }
                }.start()
            } else {
                dismiss()
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        links = requireArguments().getStringArray("links")!!
        downloadTitle = requireArguments().getString("download_title")!!
        mimeTypeOfFilesToDownload = requireArguments().getString("mime_type")!!
        val numberOfFiles = requireArguments().getInt("number_of_files")
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.download_approvement)
            .setIcon(R.drawable.baseline_download_36)
            .setMessage(resources.getString(R.string.download_notice, numberOfFiles))
            .setPositiveButton(R.string.download_ok, listener)
            .setNegativeButton(R.string.download_cancel, null)
            .create()
    }
}