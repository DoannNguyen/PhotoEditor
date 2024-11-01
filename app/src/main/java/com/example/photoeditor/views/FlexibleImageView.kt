package com.example.photoeditor.views
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import android.os.RemoteException
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.example.photoeditor.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class FlexibleImageView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private var bitmap: Bitmap? = null
    private val paint: Paint = Paint()
    private var currentMode: Mode = Mode.NONE
    private var drawingView: DrawingView? = null
    private var cropImageView: CroppingView? = null
    private var bitmapOut: Bitmap? = null
    private var isDrawingMode = false

    enum class Mode {
        CROP, DRAW, VIEW, NONE
    }
    enum class Action {
        SaveDrawing, SaveCropped
    }

    init {
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
    }

    fun setBitmap(resourceId: Bitmap) {
        bitmap = resourceId
        invalidate()
    }

    fun setMode(mode: Mode) {
        currentMode = mode
        removeAllViews()
        when (mode) {
            Mode.CROP -> addCropLayout()
            Mode.DRAW -> addDrawLayout()
            Mode.VIEW -> addViewLayout()
            Mode.NONE -> {}
        }
    }

    fun setAction(action: Action){
        when(action){
            Action.SaveDrawing -> saveDrawing("drawing_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(Date())}")
            Action.SaveCropped -> saveCroppedBitmap("crop_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(Date())}")
        }
    }

    @SuppressLint("MissingInflatedId", "SimpleDateFormat")
    private fun addCropLayout() {
        val cropLayout = LayoutInflater.from(context).inflate(R.layout.crop_layout, this, false)
        cropImageView = cropLayout.findViewById<CroppingView>(R.id.img_cropMode)
        cropImageView?.setBitmap(bitmap!!)
        isDrawingMode = false
        addView(cropLayout)
    }

    @SuppressLint("MissingInflatedId", "SimpleDateFormat")
    private fun addDrawLayout() {
        val drawLayout = LayoutInflater.from(context).inflate(R.layout.draw_layout, this, false)
        drawingView = drawLayout.findViewById(R.id.img_drawMode)
        try {
            isDrawingMode = true
            drawingView?.setBitmap(bitmap!!)
            addView(drawLayout)
        }catch (e: RemoteException){
            e.printStackTrace()
        }

    }

    private fun addViewLayout() {
        val viewLayout = LayoutInflater.from(context).inflate(R.layout.view_layout, this, false)
        val imageView = viewLayout.findViewById<ImageView>(R.id.img_viewMode)
        if (bitmapOut != null && isDrawingMode){
            isDrawingMode = false
            imageView.setImageBitmap(bitmapOut)
            addView(viewLayout)
        }else{
            imageView.setImageBitmap(bitmap)
            addView(viewLayout)
        }
    }

     private fun saveDrawing(fileName: String): Boolean {

        val bitmap = Bitmap.createBitmap(width, height , Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        bitmapOut = bitmap

        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val file = File(directory, "$fileName.jpg")

        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out) // Lưu dưới dạng JPEG
            }
            Toast.makeText(context, "Đã lưu ảnh", Toast.LENGTH_SHORT).show()
            setBitmap(bitmap)
            setMode(Mode.VIEW)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }

    }

    private fun saveCroppedBitmap(fileName: String): Boolean {
        val croppedBitmap = cropImageView?.cropBitmap()
        return if (croppedBitmap != null) {
            saveBitmapToFile(croppedBitmap, fileName)
            Toast.makeText(context, "Đã lưu ảnh", Toast.LENGTH_SHORT).show()
            setBitmap(croppedBitmap)
            setMode(Mode.VIEW)
            true
        } else {
            false
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String): Boolean {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val file = File(directory, "$fileName.jpg")

        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            true // Lưu thành công
        } catch (e: IOException) {
            e.printStackTrace()
            false // Lưu thất bại
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

}