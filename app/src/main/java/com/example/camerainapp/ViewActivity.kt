package com.example.camerainapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.camerainapp.databinding.ActivityViewBinding
import java.io.File

class ViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var recyclerView = binding.rv
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "date")
        var dataAdapter = DataAdapter(directory)
        recyclerView.adapter = dataAdapter

        val lm = LinearLayoutManager(this)
        lm.orientation = LinearLayoutManager.VERTICAL

        recyclerView.layoutManager = lm

        Thread {
            Thread.sleep(1000)
            runOnUiThread {
                dataAdapter.notifyDataSetChanged()
            }
        }.start()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, CameraActivity::class.java))
        finish()
    }
}