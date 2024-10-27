package com.example.photoeditor

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.MediaColumns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.photoeditor.adapters.ImageAdapter
import com.example.photoeditor.databinding.ActivityImagesBinding

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

        binding.rcvImages.adapter = ImageAdapter(getImagesPath(this), count)
        binding.rcvImages.layoutManager = GridLayoutManager(this, 4)

    }

    @SuppressLint("Recycle")
    fun getImagesPath(activity: Activity): ArrayList<String>
    {
        val imagesPath = ArrayList<String>()

        val uri: Uri? = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projector: Array<String> = arrayOf(MediaColumns.DATA, MediaColumns.BUCKET_DISPLAY_NAME)
        val cursor = activity.contentResolver.query(uri!!, projector, null, null, null)
        val columnIndexData = cursor!!.getColumnIndexOrThrow(MediaColumns.DATA)
        //val colum_index_folderName = cursor.getColumnIndexOrThrow(MediaColumns.BUCKET_DISPLAY_NAME)
        while (cursor.moveToNext()){
            val pathImage = cursor.getString(columnIndexData)
            imagesPath.add(pathImage!!)
        }
        return imagesPath
    }
}