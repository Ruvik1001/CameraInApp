package com.example.MyPhoto

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView
import java.io.File

class DateAdapter(private val directory: File) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private val imageFiles: List<File>

    init {
        imageFiles = directory.listFiles { file ->
            file.isFile && file.extension.equals("jpg", ignoreCase = true)
        }?.toList() ?: emptyList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
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
        private val imageView: ImageView = itemView.findViewById(R.id.data)

        fun bind(imageFile: File) {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            imageView.setImageBitmap(bitmap)
        }
    }
}

