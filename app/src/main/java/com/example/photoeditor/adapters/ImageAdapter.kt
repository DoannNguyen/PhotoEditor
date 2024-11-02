package com.example.photoeditor.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.photoeditor.R
import com.example.photoeditor.interfaces.IOnItemClickListener

class ImageAdapter ( private val images :ArrayList<String>, private var count : Int, private val click: IOnItemClickListener) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private val checkedItems = BooleanArray(images.size)
    private var isCheckboxVisible = false

    @SuppressLint("NotifyDataSetChanged")
    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val image : ImageView = view.findViewById(R.id.imgItem)
        val cb : CheckBox = view.findViewById(R.id.cbItem)


        init {
            view.setOnLongClickListener {
                isCheckboxVisible = true
                checkedItems[adapterPosition] = true
                count +=1
                notifyDataSetChanged()
                true
            }

            cb.setOnCheckedChangeListener { _, isChecked ->
                checkedItems[adapterPosition] = isChecked
                count = if (isChecked) count + 1 else count - 1
            }

            itemView.setOnClickListener {
                if (isCheckboxVisible) {
                    cb.isChecked = !cb.isChecked
                    count = if (cb.isChecked) count + 1 else count - 1
                }else{
                    click.onClick(adapterPosition)
                }
                if (count == 0){
                    isCheckboxVisible = false
                    notifyDataSetChanged()
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view  = LayoutInflater.from(parent.context).inflate(R.layout.image_item_layout,parent,false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setImageURI(Uri.parse(images[position]))

        holder.cb.visibility = if (isCheckboxVisible) View.VISIBLE else View.GONE
        holder.cb.isChecked = checkedItems[position]
    }
}