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

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao() : LessonDao
    abstract fun noteDao() : NoteDao


    companion object {
        @Provides
        fun getInstance(@ApplicationContext context : Context) : AppDatabase{
            val callback = object : Callback(){
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL(UPDATE_DAY_TRIGGER_1)
                    db.execSQL(UPDATE_DAY_TRIGGER_2)
                }
            }
            return Room.databaseBuilder(context, AppDatabase::class.java, "database")
                .addCallback(callback)
                .build()
        }

        const val UPDATE_DAY_TRIGGER_1 =
            "CREATE TRIGGER IF NOT EXISTS update_day_stage1 " +
                    "BEFORE INSERT ON tblSchedule " +
                    "BEGIN " +
                    "DELETE FROM tblSchedule WHERE groupName = NEW.groupName AND " +
                    "                lessonDate = NEW.lessonDate AND " +
                    "                lessonNumber = NEW.lessonNumber;" +
                    "END;"
        const val UPDATE_DAY_TRIGGER_2 =
            "CREATE TRIGGER IF NOT EXISTS update_day_stage2 " +
                    "AFTER INSERT ON tblSchedule " +
                    "BEGIN " +
                    "DELETE FROM tblSchedule WHERE subjectName = '';"+
                    "END;"
    }
}