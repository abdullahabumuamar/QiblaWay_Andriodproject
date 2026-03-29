import android.app.Activity
import android.view.ViewGroup
import android.widget.Toast
import java.util.ArrayList

class prayerAdapter(var activity: Activity,var data:ArrayList<Pra>) {
}


class ProductAdapter(var activity: Activity,var data:ArrayList<Product>):RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {
    class MyViewHolder(var binding: ProductitemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding=ProductitemBinding.inflate(activity.layoutInflater,parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.tvId.text=data[position].id.toString()
        holder.binding.tvName.text=data[position].name
        holder.binding.tvprice.text=data[position].price.toString()
        holder.binding.tvImage.setImageResource(data[position].img)
        holder.binding.root.setOnClickListener {
            Toast.makeText(activity,data[position].name,Toast.LENGTH_SHORT).show()
        }


    }

    override fun getItemCount(): Int {
        return  data.size
    }
}