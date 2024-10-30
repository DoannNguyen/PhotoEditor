package com.example.photoeditor.views
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater

import android.widget.FrameLayout
import android.widget.ImageView
import com.example.photoeditor.R

class FlexibleImageView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private var bitmap: Bitmap? = null
    private val paint: Paint = Paint()
    private var currentMode: Mode = Mode.NONE

    enum class Mode {
        CROP, DRAW, VIEW, NONE
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
            Mode.NONE -> {} // Không làm gì
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun addCropLayout() {
        val cropLayout = LayoutInflater.from(context).inflate(R.layout.crop_layout, this, false)
        val imageView = cropLayout.findViewById<CropImageView>(R.id.img_cropMode)
        imageView.setBitmap(bitmap!!)
        addView(cropLayout)
    }

    @SuppressLint("MissingInflatedId")
    private fun addDrawLayout() {
        val drawLayout = LayoutInflater.from(context).inflate(R.layout.draw_layout, this, false)
        val imageView = drawLayout.findViewById<DrawingView>(R.id.img_drawMode)
        imageView.setBitmap(bitmap!!)
        addView(drawLayout)
    }

    private fun addViewLayout() {
        val viewLayout = LayoutInflater.from(context).inflate(R.layout.view_layout, this, false)
        val imageView = viewLayout.findViewById<ImageView>(R.id.img_viewMode)
        imageView.setImageBitmap(bitmap)
        addView(viewLayout)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }
}