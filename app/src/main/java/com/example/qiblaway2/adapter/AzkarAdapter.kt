package com.example.qiblaway2.adapter



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qiblaway2.R
import com.example.qiblaway2.model.AzkarModel

class AzkarAdapter(private var azkarList: List<AzkarModel>) :
    RecyclerView.Adapter<AzkarAdapter.AzkarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AzkarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_azkar, parent, false)
        return AzkarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AzkarViewHolder, position: Int) {
        val azkar = azkarList[position]
        holder.tvNumber.text = (position + 1).toString()
        holder.tvText.text = azkar.text
        holder.tvCount.text = "عدد التكرار: ${azkar.count}"
    }

    override fun getItemCount(): Int = azkarList.size

    fun updateList(newList: List<AzkarModel>) {
        azkarList = newList
        notifyDataSetChanged()
    }

    class AzkarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumber: TextView = itemView.findViewById(R.id.tvAzkarNumber)
        val tvText: TextView = itemView.findViewById(R.id.tvAzkarText)
        val tvCount: TextView = itemView.findViewById(R.id.tvAzkarCount)
    }
}