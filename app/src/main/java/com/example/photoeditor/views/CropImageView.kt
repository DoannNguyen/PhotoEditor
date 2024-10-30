package com.example.photoeditor.views
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CropImageView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var bitmap: Bitmap? = null
    private var paint: Paint = Paint()
    private val rect = RectF()
    private var isDrawing = false

    init {
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
    }

    fun setBitmap(newBitmap: Bitmap) {
        bitmap = newBitmap
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            val scaleX = width.toFloat() / it.width
            val scaleY = height.toFloat() / it.height
            val scale = Math.min(scaleX, scaleY)

            val scaledWidth = (it.width * scale).toInt()
            val scaledHeight = (it.height * scale).toInt()

            // Vẽ bitmap với tỷ lệ đã tính toán
            canvas.drawBitmap(it, null, RectF(0f, 0f, scaledWidth.toFloat(), scaledHeight.toFloat()), null)
        }
        if (isDrawing) {
            canvas.drawRect(rect, paint)
            canvas.save()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val scaleX = width.toFloat() / (bitmap?.width ?: 1)
        val scaleY = height.toFloat() / (bitmap?.height ?: 1)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                rect.left = event.x / scaleX
                rect.top = event.y / scaleY
                rect.right = event.x / scaleX
                rect.bottom = event.y / scaleY
                isDrawing = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                rect.right = event.x / scaleX
                rect.bottom = event.y / scaleY
            }
            MotionEvent.ACTION_UP -> {
                isDrawing = false
                cropBitmap() // Gọi hàm cắt nếu cần
            }
        }
        invalidate() // Yêu cầu vẽ lại
        return true
    }

    private fun cropBitmap() {
        bitmap?.let {
             Bitmap.createBitmap(it,
                rect.left.toInt(),
                rect.top.toInt(),
                (rect.right - rect.left).toInt(),
                (rect.bottom - rect.top).toInt())
            // Bạn có thể gọi một hàm khác ở đây để hiển thị croppedBitmap
        }
    }
}