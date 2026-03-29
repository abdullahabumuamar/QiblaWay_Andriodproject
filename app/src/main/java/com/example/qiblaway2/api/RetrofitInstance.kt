package com.example.qiblaway2.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/v1/") // Base API URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: PrayerTimesApi by lazy {
        retrofit.create(PrayerTimesApi::class.java)
    }
}