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

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.nio.charset.Charset

/**
 * Этот класс используется для преобразования тела ответа Retrofit в Document библиотеки Jsoup.
 *
 * @author Ипатов Никита
 * @since 1.0
 */
class JsoupConverterFactory : Converter.Factory(){
    override fun responseBodyConverter(type: Type,
                                       annotations: Array<Annotation>,
                                       retrofit: Retrofit): Converter<ResponseBody, *>? {
        return when (type) {
            Document::class.java -> JsoupConverter(retrofit.baseUrl().toString())
            else -> null
        }
    }

    private class JsoupConverter(val baseUri: String) : Converter<ResponseBody, Document?> {

        override fun convert(value: ResponseBody): Document? {
            val charset = value.contentType()?.charset() ?: Charset.forName("UTF-8")

            val parser = when (value.contentType().toString()) {
                "application/xml", "text/xml" -> Parser.xmlParser()
                else -> Parser.htmlParser()
            }

            return Jsoup.parse(value.byteStream(), charset.name(), baseUri, parser)
        }

    }
}