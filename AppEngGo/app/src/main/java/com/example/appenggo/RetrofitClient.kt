package com.example.appenggo

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Cập nhật IP khớp với máy Backend của bạn (192.168.100.56)
    private const val SERVER_IP = "192.168.2.6"
    private const val BASE_URL = "http://$SERVER_IP:8080/"
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor { message ->
            Log.d("Retrofit_Log", message)
        }
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // Cấu hình Gson để gửi cả các trường null lên server
    private val gson = GsonBuilder()
        .serializeNulls()
        .create()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
