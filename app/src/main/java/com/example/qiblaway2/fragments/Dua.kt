package com.example.qiblaway2.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.qiblaway2.adapter.AzkarAdapter
import com.example.qiblaway2.databinding.FragmentDuaBinding
import com.example.qiblaway2.model.AzkarModel
import org.json.JSONObject
import java.io.IOException

class Dua : Fragment() {
    private lateinit var binding: FragmentDuaBinding
    private lateinit var azkarAdapter: AzkarAdapter
    private var morningAzkar: List<AzkarModel> = listOf()
    private var eveningAzkar: List<AzkarModel> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDuaBinding.inflate(inflater, container, false)

        loadAzkarFromAssets()

        azkarAdapter = AzkarAdapter(morningAzkar)
        binding.azkarRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.azkarRecyclerView.adapter = azkarAdapter

        binding.tvMorningCount.text = "${morningAzkar.size} ذكر"
        binding.tvEveningCount.text = "${eveningAzkar.size} ذكر"

        binding.cardMorning.setOnClickListener {
            azkarAdapter.updateList(morningAzkar)
        }
        binding.cardEvening.setOnClickListener {
            azkarAdapter.updateList(eveningAzkar)
        }

        return binding.root
    }

    private fun loadAzkarFromAssets() {
        try {
            val inputStream = requireContext().assets.open("adhkar.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            morningAzkar = jsonObject.getJSONArray("morning_azkar").let { array ->
                List(array.length()) { i ->
                    val obj = array.getJSONObject(i)
                    AzkarModel(
                        id = obj.getInt("id"),
                        text = obj.getString("text"),
                        count = obj.getInt("count")
                    )
                }
            }

            eveningAzkar = jsonObject.getJSONArray("evening_azkar").let { array ->
                List(array.length()) { i ->
                    val obj = array.getJSONObject(i)
                    AzkarModel(
                        id = obj.getInt("id"),
                        text = obj.getString("text"),
                        count = obj.getInt("count")
                    )
                }
            }
        } catch (e: IOException) {
            morningAzkar = listOf()
            eveningAzkar = listOf()
        }
    }
}