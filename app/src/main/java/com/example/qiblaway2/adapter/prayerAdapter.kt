package com.example.qiblaway2.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.qiblaway2.databinding.PrayeritemBinding
import com.example.qiblaway2.model.Prayer

class prayerAdapter(var activity: Activity,var data:ArrayList<Prayer>):RecyclerView.Adapter<prayerAdapter.MyViewHolder>() {
    class MyViewHolder(var binding: PrayeritemBinding):RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding= PrayeritemBinding.inflate(activity.layoutInflater,parent,false)
       return MyViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val imageResource = data[position].image

        // تحميل الصورة باستخدام Glide
        Glide.with(activity)
            .load(imageResource)
            .into(holder.binding.imageView)

        holder.binding.name.text=data[position].name
        holder.binding.hour.text=data[position].hour.toString()
//        holder.binding.imageView.setImageResource(data[position].image)




    }

    override fun getItemCount(): Int {
        return data.size
    }


}

