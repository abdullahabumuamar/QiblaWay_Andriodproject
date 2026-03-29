package com.example.qiblaway2.adapter

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.qiblaway2.databinding.LanguageitemBinding
import com.example.qiblaway2.model.Language
import com.example.qiblaway2.utils.LanguageHelper


class LanguageAdapter(
    var activity: Activity,
    var data: ArrayList<Language>,
    private val onClick: (Language) -> Unit // Callback function when an item is clicked
) : RecyclerView.Adapter<LanguageAdapter.MyViewHolder>() {

    private var selectedPosition: Int = -1 // To track the selected item

    init {
        // Retrieve the selected position from SharedPreferences
        val sharedPref = activity.getSharedPreferences("MyPrefs", MODE_PRIVATE)
        selectedPosition = sharedPref.getInt("selectedPosition2", 0) // Englsih Defualt
    }

    class MyViewHolder(var binding: LanguageitemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = LanguageitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val language = data[position]

        holder.binding.languageName.text = language.name
        holder.binding.languageImg.setImageResource(language.img)

        // Check if this item is selected
        if (holder.adapterPosition == selectedPosition) {
            holder.binding.root.strokeWidth = 8
            holder.binding.root.strokeColor = Color.BLACK
        } else {
            holder.binding.root.strokeWidth = 0
            holder.binding.root.strokeColor = Color.TRANSPARENT
        }

        // Set onClick listener for each item
        holder.binding.root.setOnClickListener {
            selectedPosition = holder.adapterPosition // Update selected position
            notifyDataSetChanged() // Refresh the RecyclerView
        //    Toast.makeText(activity,data[position].name, Toast.LENGTH_SHORT).show()
            changeLanguage(data[position].idS)

            // Save the selected position to SharedPreferences
            val sharedPref = activity.getSharedPreferences("MyPrefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("selectedPosition2", selectedPosition) // Save selected position
                apply() // Apply the changes
            }

            onClick(language) // Execute the callback when an item is clicked
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun changeLanguage(languageCode: String) {
        activity?.let {
            LanguageHelper.setLocale(languageCode, it)
            it.recreate() // إعادة تشغيل الـ Activity لتحديث اللغة
        }
    }
}
