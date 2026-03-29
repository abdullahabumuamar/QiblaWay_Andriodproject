package com.example.qiblaway2.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.qiblaway2.R
import com.example.qiblaway2.adapter.LanguageAdapter
import com.example.qiblaway2.databinding.FragmentLanguageBinding
import com.example.qiblaway2.model.Language



class Language : Fragment() {
   lateinit var binding: FragmentLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentLanguageBinding.inflate(layoutInflater,container,false)

        val data=ArrayList<Language>()
        data.add(Language(1,getString(R.string.language1),R.drawable.br,"en"))
        data.add(Language(2,getString(R.string.language2),R.drawable.sa,"ar"))
        data.add(Language(3,getString(R.string.language3 ),R.drawable.ru,"ru"))
        data.add(Language(3,getString(R.string.language4 ),R.drawable.kz,"kk"))


        val languageAdapter= LanguageAdapter(requireActivity(),data){selectedLanguage->
         //   Toast.makeText(requireActivity(),selectedLanguage.name+"${selectedLanguage.id}", Toast.LENGTH_SHORT).show()

        }
        binding.rvLang.adapter=languageAdapter
        binding.rvLang.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL) //two columns
        return binding.root
    }


}