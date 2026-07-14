package com.example.userlistapp.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object {
        fun gerRetrofitInstance(): Retrofit {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
            client.addInterceptor(logging)
            return Retrofit.Builder()
                .baseUrl("https://dummyjson.com/")
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        fun getApiService(): ApiService {
            return gerRetrofitInstance().create(ApiService::class.java)
        }
    }


}


