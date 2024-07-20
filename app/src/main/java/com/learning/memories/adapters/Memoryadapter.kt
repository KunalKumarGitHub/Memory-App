package com.learning.memories.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.learning.memories.R
import com.learning.memories.activities.AddMemoryActivity
import com.learning.memories.activities.MainActivity
import com.learning.memories.database.DatabaseHandler
import com.learning.memories.models.MemoryModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//open class memoryAdapter(
//    private val context: Context,
//    private var list: ArrayList<MemoryModel>
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    private var onClickListener: OnClickListener?=null
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//
//        return MyViewHolder(
//            LayoutInflater.from(context).inflate(
//                R.layout.item_memory,
//                parent,
//                false
//            )
//        )
//    }
//
//    fun setOnCLickListener(onClickListener: OnClickListener){
//        this.onClickListener = onClickListener
//    }
//    @RequiresApi(Build.VERSION_CODES.P)
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val model = list[position]
//        if (holder is MyViewHolder) {
//            holder.itemView.requireViewById<ImageView>(R.id.iv_place_image).setImageURI(Uri.parse(model.image))
//            holder.itemView.requireViewById<TextView>(R.id.tvTitle).text=model.title
//            holder.itemView.requireViewById<TextView>(R.id.tvDescription).text=model.description
//            holder.itemView.setOnClickListener {
//                if(onClickListener!=null){
//                    onClickListener!!.onClick(position,model)
//                }
//            }
//        }
//    }
//
//    fun notifyEditItem(activity: Activity, position:Int, requestCode:Int){
//        val intent= Intent(context, AddMemoryActivity::class.java)
//        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
//        activity.startActivityForResult(intent,requestCode)
//        notifyItemChanged(position)
//    }
//
//    fun notifyDeleteItem(position: Int) {
//        val dbHandler= DatabaseHandler(context)
//        val isDeleted = dbHandler.deleteHappyPlace(list[position])
//        if(isDeleted>0){
//            list.removeAt(position)
//            notifyItemChanged(position)
//        }
//    }
//    override fun getItemCount(): Int {
//        return list.size
//    }
//
//    interface OnClickListener{
//        fun onClick(position:Int,model:MemoryModel)
//    }
//    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
//}
open class memoryAdapter(
    private val context: Context,
    private var list: ArrayList<MemoryModel>
) : ListAdapter<MemoryModel,memoryAdapter.MyViewHolder>(diffUtil()) {

    private var onClickListener:OnClickListener?=null

    class MyViewHolder(view: View):RecyclerView.ViewHolder(view){
        private val image: ImageView =view.findViewById(R.id.iv_place_image)
        private val title: TextView =view.findViewById(R.id.tvTitle)
        private val description: TextView =view.findViewById(R.id.tvDescription)

        fun bind(item:MemoryModel){
            image.setImageURI(Uri.parse(item.image))
            title.text=item.title
            description.text=item.description
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_memory,
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]
        holder.bind(model)
        holder.itemView.setOnClickListener {
            if(onClickListener!=null){
                onClickListener!!.onClick(position,model)
            }
        }
    }

    interface OnClickListener{
        fun onClick(position:Int,model:MemoryModel)
    }

    fun notifyDeleteItem(position: Int) {
        val dbHandler= DatabaseHandler(context)
        val isDeleted = dbHandler.deleteHappyPlace(list[position])
        if(isDeleted>0){
            list.removeAt(position)
            notifyItemChanged(position)
        }
    }

    fun notifyEditItem(activity: Activity, position:Int, requestCode:Int){
        val intent= Intent(context, AddMemoryActivity::class.java)
        synchronized(this){
            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        }
        activity.startActivityForResult(intent,requestCode)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnCLickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    class diffUtil:DiffUtil.ItemCallback<MemoryModel>(){
        override fun areItemsTheSame(oldItem: MemoryModel, newItem: MemoryModel): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: MemoryModel, newItem: MemoryModel): Boolean {
            return oldItem==newItem
        }

    }
}