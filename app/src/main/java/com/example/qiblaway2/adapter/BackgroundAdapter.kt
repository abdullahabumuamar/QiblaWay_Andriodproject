package com.example.qiblaway2.adapter

import android.app.Activity
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.qiblaway2.R
import com.example.qiblaway2.databinding.BackgrounditemBinding
class BackgroundAdapter(
    private var activity: Activity,
    private var data: List<Int>,
    private val onClick: (Int) -> Unit // تمرير دالة عند النقر
) : RecyclerView.Adapter<BackgroundAdapter.MyViewHolder>() {

    private var selectedPosition: Int = -1 // لتتبع العنصر المحدد
    init {
        // استرجاع القيمة المخزنة من SharedPreferences عند تهيئة الـ Adapter
        val sharedPref = activity.getSharedPreferences("MyPrefs",MODE_PRIVATE)
        selectedPosition = sharedPref.getInt("selectedPosition", 0)// Default value is 0 (first item)
    }

    class MyViewHolder(var binding: BackgrounditemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = BackgrounditemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val imageResource = data[position]

        // تحميل الصورة باستخدام Glide
        Glide.with(activity)
            .load(imageResource)
            .into(holder.binding.HomeWallPaper)

        // التحقق من العنصر المحدد باستخدام adapterPosition
        if (holder.adapterPosition == selectedPosition) {
            holder.binding.imgtrue.visibility = View.VISIBLE
        } else {
            holder.binding.imgtrue.visibility = View.GONE
        }

        // تعيين حدث النقر على الصورة
        holder.binding.HomeWallPaper.setOnClickListener {
            val dailog= Dialog(activity)
            dailog.setContentView(R.layout.wallpaper_alert)
            dailog.setCancelable(false)
            val btnSetAlert=dailog.findViewById<Button>(R.id.btnSetAlert)
            val btnCancelAlert=dailog.findViewById<Button>(R.id.btnCancelAlert)
            val imgAlert=dailog.findViewById<ImageView>(R.id.imgAlert)
            imgAlert.setImageResource(imageResource)

            btnSetAlert.setOnClickListener {
                selectedPosition = holder.adapterPosition // استخدام adapterPosition هنا
                notifyDataSetChanged() // تحديث الـ RecyclerView
                // حفظ الموضع المحدد في SharedPreferences
                val sharedPref = activity.getSharedPreferences("MyPrefs", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putInt("selectedPosition", selectedPosition) // تخزين الموضع المحدد
                    apply() // تطبيق التغييرات
                }
                onClick(imageResource) // تنفيذ الإجراء عند النقر
                dailog.dismiss()
            }
            btnCancelAlert.setOnClickListener {
                dailog.dismiss()
            }

            dailog.show()

            //**********

        }
    }


    override fun getItemCount(): Int = data.size



}




