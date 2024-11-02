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
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
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
    private var isWrite = false
    private var dX: Float = 0f
    private var dY: Float = 0f
    private var editText: EditText? = null

    enum class Mode {
        CROP, DRAW, VIEW, NONE, WRITE
    }
    enum class Action {
        SaveDrawing, SaveCropped, SaveWrite, UNDO
    }

    init {
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
    }

    fun setBitmap(resourceId: Bitmap) {
        bitmap = null
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
            Mode.WRITE -> addWriteLayout()
            Mode.NONE -> {}
        }
    }

    fun setAction(action: Action){
        when(action){
            Action.SaveDrawing -> saveDrawing("drawing_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(Date())}")
            Action.SaveCropped -> saveCroppedBitmap("crop_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(Date())}")
            Action.SaveWrite -> {
                drawTextOnBitmap(editText!!.text.toString(), dX, dY)
                saveDrawing("drawing_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(Date())}")
            }
            Action.UNDO -> drawingView?.removeLastPath()
        }
    }

    @SuppressLint("MissingInflatedId", "SimpleDateFormat")
    private fun addCropLayout() {
        val cropLayout = LayoutInflater.from(context).inflate(R.layout.crop_layout, this, false)
        cropImageView = cropLayout.findViewById(R.id.img_cropMode)
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
        }else if(bitmapOut != null && isWrite){
            isWrite = false
            imageView.setImageBitmap(bitmapOut)
            addView(viewLayout)
        }else{
            imageView.setImageBitmap(bitmap)
            addView(viewLayout)
        }
    }

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    private fun addWriteLayout() {
        val view = LayoutInflater.from(context).inflate(R.layout.write_text_layout, this, false)
        editText = view.findViewById<EditText>(R.id.editText)
        val imageView = view.findViewById<ImageView>(R.id.img_writeMode)
        editText!!.setOnTouchListener { v, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - motionEvent.rawX
                    dY = v.y - motionEvent.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = motionEvent.rawX + dX
                    val newY = motionEvent.rawY + dY
                    v.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start()
                }
                else -> return@setOnTouchListener false

            }
            true
        }
        editText!!.text.clear()
        editText!!.isFocusableInTouchMode = true
        editText!!.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        imageView.setImageBitmap(bitmap!!)
        addView(view)
    }

    private fun drawTextOnBitmap(text: String, x: Float, y: Float) {
        val canvas = Canvas(bitmap!!)

        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        isWrite = true
        canvas.drawText(text, x, y, paint)
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
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