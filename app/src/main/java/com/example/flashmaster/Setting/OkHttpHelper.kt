package com.example.flashmaster.Setting

import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

object OkHttpHelper {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    fun makeSampleGetRequest(url: String = "https://jsonplaceholder.typicode.com/todos/1") {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OkHttpHelper", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e("OkHttpHelper", "Unexpected code $it")
                    } else {
                        Log.d("OkHttpHelper", "Response: ${it.body?.string()}")
                    }
                }
            }
        })
    }
} 