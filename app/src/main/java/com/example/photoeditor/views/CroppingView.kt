package com.example.photoeditor.views
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CroppingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var bitmap: Bitmap? = null
    private var paint: Paint = Paint()
    private var cropRect: RectF = RectF()
    private var isDrawing = false

    init {
        paint.apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true
        }
    }

    fun setBitmap(newBitmap: Bitmap) {
        bitmap = newBitmap

        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {

            val viewWidth = width
            val viewHeight = height
            val scale = (viewWidth.toFloat() / it.width).coerceAtMost(viewHeight.toFloat() / it.height)
            val dx = (viewWidth - it.width * scale) / 2f
            val dy = (viewHeight - it.height * scale) / 2f

            // Vẽ bitmap
            canvas.drawBitmap(it, null, RectF(dx, dy, dx + it.width * scale, dy + it.height * scale), null)
        }

        canvas.drawRect(cropRect, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val x = it.x
            val y = it.y

            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDrawing = true
                    cropRect.left = x
                    cropRect.top = y
                    cropRect.right = x
                    cropRect.bottom = y

                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDrawing) {
                        cropRect.right = x
                        cropRect.bottom = y
                    }
                }
                MotionEvent.ACTION_UP -> {
                    isDrawing = false

                    cropBitmap()
                }
            }
            invalidate()
        }

        return true
    }

    fun cropBitmap(): Bitmap? {
        bitmap?.let { bmp ->
            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()
            val scale = (viewWidth / bmp.width).coerceAtMost(viewHeight / bmp.height)

            val x = ((cropRect.left - (viewWidth - bmp.width * scale) / 2) / scale).toInt()
            val y = ((cropRect.top - (viewHeight - bmp.height * scale) / 2) / scale).toInt()
            val width = ((cropRect.right - cropRect.left) / scale).toInt()
            val height = ((cropRect.bottom - cropRect.top) / scale).toInt()

            return Bitmap.createBitmap(bmp, x, y, width.coerceAtLeast(0), height.coerceAtLeast(0))
        }
        return null
    }

        fun clearCropRect() {
        cropRect.setEmpty()
        invalidate()
    }
}