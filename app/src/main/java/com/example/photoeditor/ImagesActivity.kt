package com.example.photoeditor

import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.photoeditor.MainActivity.Companion.REQUIRED_PERMISSIONS
import com.example.photoeditor.adapters.ImageAdapter
import com.example.photoeditor.databinding.ActivityImagesBinding
import com.example.photoeditor.interfaces.IOnItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagesBinding
    private var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rcvImages.adapter = ImageAdapter(getImagesPath(this), count, object : IOnItemClickListener {
            override fun onClick(position: Int) {
                setResult(Activity.RESULT_OK, Intent().apply {
                    Log.e("TAG", "onClick: ${Uri.parse(getImagesPath(this@ImagesActivity)[position])}")
                    data = Uri.parse(getImagesPath(this@ImagesActivity)[position])
                })

                finish()
            }
        })
        binding.rcvImages.layoutManager = GridLayoutManager(this, 4)

    }

    private fun getImagesPath(activity: Activity): ArrayList<String> {
        val imagesPath = ArrayList<String>()

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = activity.contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val pathImage = cursor.getString(columnIndexData)
                imagesPath.add(pathImage)
            }
        }

        return imagesPath
    }


}