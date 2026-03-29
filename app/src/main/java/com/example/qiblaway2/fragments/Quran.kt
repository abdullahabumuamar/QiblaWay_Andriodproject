package com.example.qiblaway2.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.qiblaway2.databinding.FragmentQuranBinding
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.qiblaway2.adapter.QuranSurahAdapter
import android.text.Editable
import android.text.TextWatcher
import com.example.qiblaway2.R
import androidx.navigation.fragment.findNavController


class Quran : Fragment() {
    lateinit var binding: FragmentQuranBinding
    lateinit var adapter: QuranSurahAdapter
    lateinit var surahNames: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuranBinding.inflate(layoutInflater, container, false)

        // Create a list of pairs (number, name)
        val surahList = (1..114).map { it to getString(resources.getIdentifier("surah_$it", "string", requireContext().packageName)) }
        var filteredList = surahList

        adapter = QuranSurahAdapter(filteredList) { surahNumber, surahName ->
            val bundle = Bundle()
            bundle.putInt("surahNumber", surahNumber)
            bundle.putString("surahName", surahName)
            findNavController().navigate(R.id.surahDetailsFragment2, bundle)
        }
        binding.rvSurahs.adapter = adapter
        binding.rvSurahs.layoutManager = LinearLayoutManager(requireContext())

        // Enable search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                filteredList = if (query.isEmpty()) {
                    surahList
                } else if (query.all { it.isDigit() }) {
                    val idx = query.toIntOrNull()
                    if (idx != null && idx in 1..114) surahList.filter { it.first == idx } else emptyList()
                } else {
                    surahList.filter { it.second.contains(query, ignoreCase = true) }
                }
                adapter.updateList(filteredList)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }


}