package com.example.qiblaway2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qiblaway2.R

class QuranSurahAdapter(
    private var surahList: List<Pair<Int, String>>,
    private val onSurahClick: (Int, String) -> Unit
) : RecyclerView.Adapter<QuranSurahAdapter.SurahViewHolder>() {

    inner class SurahViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSurahName: TextView = itemView.findViewById(R.id.tvSurahName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_surah, parent, false)
        return SurahViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurahViewHolder, position: Int) {
        val (surahNumber, surahName) = surahList[position]
        holder.tvSurahName.text = surahName
        holder.itemView.setOnClickListener {
            holder.itemView.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    holder.itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    onSurahClick(surahNumber, surahName)
                }
                .start()
        }
    }

    override fun getItemCount(): Int = surahList.size

    fun updateList(newList: List<Pair<Int, String>>) {
        surahList = newList
        notifyDataSetChanged()
    }
} 