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

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

/**
 * Этот класс используется для описания единичной сущности заметок.
 * Используется в ORM.
 * Содержит поля для даты, группы, темы, текста, идентификатора фото.
 *
 * @author  Ипатов Никита
 * @since 1.0
 */
@Entity(tableName = "tblNote")
data class Note(
    @PrimaryKey(autoGenerate = true) val id : Int,
    @ColumnInfo(name = "noteDate") var date : Calendar,
    @ColumnInfo(name = "noteGroup") var group : String,
    @ColumnInfo(name = "noteTheme") var theme: String?,
    @ColumnInfo(name = "noteText") var text: String,
    @ColumnInfo(name = "notePhotoIDs") var photoIDs: ArrayList<Uri>?
){
    override fun toString(): String {
        val resources = ScheduleApp.getInstance().resources
        var res = ""
        res = res + resources.getString(R.string.date) + ": " + DateConverters().toString(date) + "\n"
        res = res + resources.getString(R.string.group) + ": " + group + "\n"
        res = res + resources.getString(R.string.theme) + ": " + theme + "\n"
        res = res + resources.getString(R.string.text) + ": " + text + "\n"
        return res
    }
}