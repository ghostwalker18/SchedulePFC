package com.ghostwalker18.schedulepfc

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Этот класс используется для ORM.
 * Содержит методы для преобразования ArrayList of Uri в String для БД и наоборот
 *
 * @author  Ипатов Никита
 * @since 1.0
 */
class PhotoURIArrayConverters {
    /**
     * Этот метод преобразует ArrayList of Uri сущности в String для БД.
     *
     * @param uris  the entity attribute value to be converted
     * @return
     */
    @TypeConverter
    fun toString(uris: ArrayList<Uri>?): String? {
        return try{
            val gson = GsonBuilder()
                .registerTypeAdapter(Uri::class.java, UriJsonAdapter())
                .create()
            if (uris.isNullOrEmpty()) return null
            val listType = object : TypeToken<java.util.ArrayList<Uri?>?>() {}.type
            gson.toJson(uris, listType)
        } catch (e: Exception){
            null
        }
    }

    /**
     * Этот метод преобразует String из БД в ArrayList of Uri сущности.
     *
     * @param uriString  the data from the database column to be converted
     * @return
     */
    @TypeConverter
    fun fromString(uriString: String?): ArrayList<Uri>? {
        return try{
            val gson = GsonBuilder()
                .registerTypeAdapter(Uri::class.java, UriJsonAdapter())
                .create()
            if (uriString == null) return null
            val listType = object : TypeToken<java.util.ArrayList<Uri?>>() {}.type
            gson.fromJson(uriString, listType)
        } catch (e: Exception){
            null
        }

    }

    /**
     * Этот класс используется для конвертации Uri в Json и наоборот.
     *
     * @author Ипатов Никита
     * @since 3.2
     */
    private inner class UriJsonAdapter : JsonSerializer<Uri?>,
        JsonDeserializer<Uri?> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext
        ): Uri {
            return try {
                val uri = json.asString
                if (uri.isNullOrEmpty()) Uri.EMPTY
                else Uri.parse(uri)
            } catch (e: UnsupportedOperationException) {
                Uri.EMPTY
            }
        }

        override fun serialize(src: Uri?, typeOfSrc: Type, context: JsonSerializationContext
        ): JsonElement {
            return JsonPrimitive(src.toString())
        }
    }
}