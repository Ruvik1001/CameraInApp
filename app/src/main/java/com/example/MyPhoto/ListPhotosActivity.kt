package com.example.MyPhoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.MyPhoto.databinding.ActivityListPhotosBinding
import java.io.File

class ListPhotosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityListPhotosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var recyclerView = binding.recyclerView
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "date")
        var dataAdapter = DateAdapter(directory)
        recyclerView.adapter = dataAdapter

        val menager = LinearLayoutManager(this)
        menager.orientation = LinearLayoutManager.VERTICAL

        recyclerView.layoutManager = menager

        Thread {
            Thread.sleep(1000)
            runOnUiThread {
                dataAdapter.notifyDataSetChanged()
            }
        }.start()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MyCameraActivity::class.java))
        finish()
    }
}
