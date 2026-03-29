package com.example.qiblaway2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.example.qiblaway2.R


import androidx.recyclerview.widget.RecyclerView



class RadioListAdapter(
    private val items: List<MethodItem>,
    private var selectedId: Int = -1, // Store selected ID instead of position
    private val onItemSelected: (MethodItem) -> Unit // Callback function
) : RecyclerView.Adapter<RadioListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val radioButton: RadioButton = view.findViewById(R.id.radioButton)
        val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        val textViewDescription: TextView = view.findViewById(R.id.textViewDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_methods, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.textViewTitle.text = item.title
        holder.textViewDescription.text = item.description
        holder.radioButton.isChecked = (item.id == selectedId)

        holder.radioButton.setOnClickListener {
            selectedId = item.id
            onItemSelected(item) // Call the callback with selected item
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = items.size
}


