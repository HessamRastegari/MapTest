package com.application.test.utils

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.internal.bind.DateTypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.reflect.Type
import java.util.*


/**
 * Created by Arsalan Modirkhazeni on 8/22/2020.
 */
object GsonUtils {
    var gson: Gson? = null

    fun toJson(`object`: Any?): String {
        return gson!!.toJson(`object`).toString()
    }

    fun <T> stringToArray(s: String?, clazz: Class<Array<T>>): MutableList<Array<T>> {
        val arr = Gson().fromJson(s, clazz)!!
        return mutableListOf(arr)
    }

    fun <T> fromJson(json: String?, tClass: Class<T>?): T {
        return gson!!.fromJson(json, tClass)
    }

    fun <T> fromJson(json: String?, type: Type?): T {
        return gson!!.fromJson(json, type)
    }


    private class StrongAdapter<T> internal constructor(
        private val typeAdapterFactory: TypeAdapterFactory,
        private val typeToken: TypeToken<T>
    ) : TypeAdapter<T?>() {
        @Throws(IOException::class)
        override fun write(out: JsonWriter, value: T?) {
            val delegateAdapter =
                gson!!.getDelegateAdapter(typeAdapterFactory, typeToken)
            delegateAdapter.write(out, value)
        }

        @Throws(IOException::class)
        override fun read(`in`: JsonReader): T? {
            val delegateAdapter =
                gson!!.getDelegateAdapter(typeAdapterFactory, typeToken)
            val jsonToken = `in`.peek()
            return try {
                delegateAdapter.read(`in`)
            } catch (e: Exception) {
                if (e is IllegalStateException || e.cause is IllegalStateException) {
                    if (jsonToken == JsonToken.STRING) {
                        val value = `in`.nextString()
                        if (TextUtils.isEmpty(value)) {
                            return null
                        }
                    }
                }
                throw e
            }
        }

    }

    init {
        gson = GsonBuilder()
            .registerTypeAdapterFactory(object : TypeAdapterFactory {
                override fun <T> create(
                    gson: Gson,
                    typeToken: TypeToken<T>
                ): TypeAdapter<T>? {
                    val rawType = typeToken.rawType
                    return if (rawType != String::class.java) (StrongAdapter(
                        this,
                        typeToken
                    ) as TypeAdapter<T>) else null
                }
            })
            .registerTypeAdapterFactory(object : TypeAdapterFactory {
                override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                    return if (type.rawType == Date::class.java) (DateTypeAdapter() as TypeAdapter<T>) else null
                }
            })
            .create()
    }
}