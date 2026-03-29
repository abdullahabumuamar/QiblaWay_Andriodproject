package com.example.qiblaway2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.qiblaway2.R
import org.json.JSONObject

class SurahDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_surah_details, container, false)
        val tvSurahText = view.findViewById<TextView>(R.id.tvSurahText)
        val tvSurahTitle = view.findViewById<TextView>(R.id.tvSurahTitle)
        val tvSurahNumber = view.findViewById<TextView>(R.id.tvSurahNumber)
        val seekBarTextSize = view.findViewById<android.widget.SeekBar>(R.id.seekBarTextSize)
        val fabTextSize = view.findViewById<View>(R.id.fabTextSize)

        val surahNumber = arguments?.getInt("surahNumber") ?: 1
        val surahName = arguments?.getString("surahName") ?: ""
        tvSurahTitle.text = getString(R.string.surah_title, surahName)
        tvSurahNumber.text = surahNumber.toString()
        tvSurahText.text = readSurahFromJson(surahNumber)

        // SharedPreferences for saving text size
        val prefs = requireContext().getSharedPreferences("quran_prefs", 0)
        val minTextSize = 16
        val maxTextSize = 40
        val defaultTextSize = 20
        val savedTextSize = prefs.getInt("quran_text_size", defaultTextSize)
        seekBarTextSize.max = maxTextSize - minTextSize
        seekBarTextSize.progress = savedTextSize - minTextSize
        tvSurahText.textSize = savedTextSize.toFloat()

        seekBarTextSize.visibility = View.GONE
        fabTextSize.setOnClickListener {
            val newVisibility = if (seekBarTextSize.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            seekBarTextSize.visibility = newVisibility
        }

        seekBarTextSize.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val textSize = (progress + minTextSize).coerceAtLeast(minTextSize)
                tvSurahText.textSize = textSize.toFloat()
                // Save the chosen size
                prefs.edit().putInt("quran_text_size", textSize).apply()
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        return view
    }

    private fun readSurahFromJson(surahNumber: Int): String {
        return try {
            val inputStream = requireContext().assets.open("quran.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(json)
            val ayahsArray = jsonObject.getJSONArray(surahNumber.toString())
            val builder = StringBuilder()
            for (i in 0 until ayahsArray.length()) {
                val ayahObj = ayahsArray.getJSONObject(i)
                builder.append(ayahObj.getString("text")).append("\n")
            }
            builder.toString().trim()
        } catch (e: Exception) {
            "نص السورة غير متوفر."
        }
    }
} 