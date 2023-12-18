package com.example.camerainapp

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView
import java.io.File

class DataAdapter(private val directory: File) : RecyclerView.Adapter<DataAdapter.DateViewHolder>() {

    private val imageFiles: List<File>

    init {
        imageFiles = directory.listFiles { file ->
            file.isFile && file.extension.equals("jpg", ignoreCase = true)
        }?.toList() ?: emptyList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val imageFile = imageFiles[position]
        holder.bind(imageFile)
    }

    override fun getItemCount(): Int {
        return imageFiles.size
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.item)
        private val imageView: ImageView = itemView.findViewById(R.id.data)

        fun bind(imageFile: File) {
            cardView.radius = 30f

            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            imageView.setImageBitmap(bitmap)
        }
    }
}

