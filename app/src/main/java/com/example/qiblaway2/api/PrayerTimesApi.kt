package com.example.qiblaway2.api

import com.example.qiblaway2.model.PrayerTimesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PrayerTimesApi {
    @GET("timingsByCity")
    fun getPrayerTimes(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int, // Default method (ISNA)=2
        @Query("school") school: Int, // 0 = Shafi Standard, 1 = Hanafi
        @Query("date") date: String? = null // Optional date in DD-MM-YYYY format
    ): Call<PrayerTimesResponse>
    
    // Alternative method using coordinates (more reliable)
    @GET("timings")
    fun getPrayerTimesByCoordinates(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int, // Default method (ISNA)=2
        @Query("school") school: Int, // 0 = Shafi Standard, 1 = Hanafi
        @Query("date") date: String? = null // Optional date in DD-MM-YYYY format
    ): Call<PrayerTimesResponse>
}





