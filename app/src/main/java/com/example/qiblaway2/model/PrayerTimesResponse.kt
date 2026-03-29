package com.example.qiblaway2.model

data class PrayerTimesResponse (
    val data: PrayerData
)

data class PrayerData(
    val timings: Timings
)

data class Timings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)
