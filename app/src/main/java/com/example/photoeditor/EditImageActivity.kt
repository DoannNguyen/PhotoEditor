package com.example.photoeditor

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.photoeditor.databinding.ActivityEditImageBinding
import com.example.photoeditor.views.FlexibleImageView
import java.io.FileNotFoundException


class EditImageActivity : AppCompatActivity() {
    private var bitmap: Bitmap? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode == Activity.RESULT_OK){
            try {
                val data = result.data?.data
                bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(data!!))
                val inputStream = contentResolver.openInputStream(data.toString().toUri())
                val exif = ExifInterface(inputStream!!)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val adjustedBitmap = rotateBitmap(bitmap!!, orientation)
                binding.imgPreview.setBitmap(adjustedBitmap)
                binding.imgPreview.setMode(FlexibleImageView.Mode.VIEW)

            } catch (e: FileNotFoundException) {
                Log.e("TAG", "File not found: ${e.message}")
            } catch (e: Exception) {
                Log.e("TAG", "Error opening input stream: ${e.message}")
            }

        }else{
            Toast.makeText(baseContext, "Photo capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }




    private lateinit var binding: ActivityEditImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.layoutChooseImage.setOnClickListener{
            val intent = Intent(this, ImagesActivity::class.java)
//            intent.type = "image/*"
            resultLauncher.launch(intent)
        }



        binding.layoutTakePicture.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            resultLauncher.launch(intent)
        }

        if (!allPermissionsGranted()) {
            requestPermissions()
        }

        binding.btnCrop.setOnClickListener {
            if(bitmap != null){
                binding.btnDoneCrop.visibility = View.VISIBLE
                binding.imgPreview.setMode(FlexibleImageView.Mode.CROP)
            }
        }

        binding.btnDraw.setOnClickListener {
            if(bitmap != null){
                binding.btnDoneDraw.visibility = View.VISIBLE
                binding.imgPreview.setMode(FlexibleImageView.Mode.DRAW)
            }
        }

        binding.btnDoneDraw.setOnClickListener {
            binding.imgPreview.setAction(FlexibleImageView.Action.SaveDrawing)
            binding.imgPreview.setMode(FlexibleImageView.Mode.VIEW)
            binding.btnDoneDraw.visibility = View.GONE
        }
        binding.btnDoneCrop.setOnClickListener {
            binding.imgPreview.setAction(FlexibleImageView.Action.SaveCropped)
            binding.imgPreview.setMode(FlexibleImageView.Mode.VIEW)
            binding.btnDoneCrop.visibility = View.GONE
        }

    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
    { permissions ->
        // Handle Permission granted/rejected
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value)
                permissionGranted = false
        }
        if (!permissionGranted) {
            Toast.makeText(baseContext, "Permission request denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}