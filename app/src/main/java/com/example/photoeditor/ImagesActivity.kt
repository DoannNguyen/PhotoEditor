package com.example.photoeditor

import android.app.Activity
import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.photoeditor.adapters.ImageAdapter
import com.example.photoeditor.databinding.ActivityImagesBinding
import com.example.photoeditor.interfaces.IOnItemClickListener

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
                try {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        data = getUriFromFilePath(getImagesPath(this@ImagesActivity)[position])
                    })
                }catch (e: Exception){
                    e.printStackTrace()
                }
                finish()
            }
        })
        binding.rcvImages.layoutManager = GridLayoutManager(this, 3)

    }

    private fun getImagesPath(activity: Activity): ArrayList<String> {
        val imagesPath = ArrayList<String>()

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = activity.contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val columnIndexData = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                val pathImage = it.getString(columnIndexData)
                imagesPath.add(pathImage)
            }
        }

        return imagesPath
    }

    private fun getUriFromFilePath(filePath: String): Uri? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DATA} = ?"
        val selectionArgs = arrayOf(filePath)

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            }
        }
        return null
    }

}