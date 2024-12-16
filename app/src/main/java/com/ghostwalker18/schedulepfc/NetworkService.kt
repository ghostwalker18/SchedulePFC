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
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Этот класс используется для предоставления приложению услуг доступа к сети.
 *
 * @author Ipatov Nikita
 * @since 1.0
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkService {

    private val sizeOfCache: Long = 10 * 1024 * 1024

    /**
     * Этот метод позволяет получить API сайта ПТГХ.
     * @return API сайта для доступа к скачиванию файлов расписания
     */
    @Provides
    fun getScheduleAPI(@ApplicationContext context: Context): ScheduleNetworkAPI {
        val baseUri = "https://ptgh.onego.ru"
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val apiBuilder = Retrofit.Builder()
            .baseUrl(baseUri)
            .callbackExecutor(Executors.newFixedThreadPool(3))
            .addConverterFactory(JsoupConverterFactory())
        val isCachingEnabled = preferences.getBoolean("isCachingEnabled", true)
        if (isCachingEnabled) {
            val cache = Cache(File(context.cacheDir, "http"), sizeOfCache)
            val client = OkHttpClient().newBuilder()
                .cache(cache)
                .addInterceptor(CacheInterceptor())
                .build()
            apiBuilder.client(client)
        }
        return apiBuilder
            .build()
            .create(ScheduleNetworkAPI::class.java)
    }
}