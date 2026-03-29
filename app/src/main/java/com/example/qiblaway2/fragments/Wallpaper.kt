package com.example.qiblaway2.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.qiblaway2.R
import com.example.qiblaway2.adapter.BackgroundAdapter
import com.example.qiblaway2.databinding.FragmentWallpaperBinding


class Wallpaper : Fragment() {
    lateinit var binding: FragmentWallpaperBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentWallpaperBinding.inflate(layoutInflater,container,false)
        //عشان نحفظ الصور في ال SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("selectedBackground",MODE_PRIVATE)

        //All-Backgrounds
        val backgroundsAlqsa = listOf(R.drawable.jerusalem1,R.drawable.jerusalem2, R.drawable.jerusalem3
            ,R.drawable.mecca,R.drawable.mecca3,R.drawable.medina1,R.drawable.medina2,R.drawable.medina3)
        binding.rvWallpapers.adapter = BackgroundAdapter(requireActivity(), backgroundsAlqsa){ selectedImage ->
            val editor = sharedPref.edit()
            editor.putInt("selectedImage", selectedImage)  // Store the selected image ID
            editor.apply()  // Apply changes to SharedPreferences

        }
        binding.rvWallpapers.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) //two columns

        return binding.root
    }


}