package com.example.photoeditor.views
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DrawingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var paint : Paint? = null
    private var path: Path? = null
    private var canvas: Canvas? = null
    private var bitmap: Bitmap? = null

    init {
        paint = Paint()
        path = Path()
        paint!!.color = Color.RED
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = 5f
        paint!!.isAntiAlias = true

    }

    fun setBitmap(newBitmap: Bitmap) {
        bitmap = newBitmap
        val mutableBitmap = newBitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvas = Canvas(mutableBitmap)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            val viewWidth = width
            val viewHeight = height

            val scale =
                (viewWidth.toFloat() / it.width).coerceAtMost(viewHeight.toFloat() / it.height)
            val dx = (viewWidth - it.width * scale) / 2f
            val dy = (viewHeight - it.height * scale) / 2f

            val matrix = Matrix().apply {
                postScale(scale, scale)
                postTranslate(dx, dy)
            }
            canvas.drawBitmap(it, matrix, null)
        }
        canvas.drawPath(path!!, paint!!)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event!!.x
        val y = event.y

        when (event.action)
        {
            MotionEvent.ACTION_DOWN -> {
                path!!.moveTo(x, y)
            }
            MotionEvent.ACTION_UP -> {

            }
            MotionEvent.ACTION_MOVE -> {
                path!!.lineTo(x, y)
            }
            else -> {

            }
        }
        invalidate()
        return true
    }

    fun removeLastPath() {
        if (path!!.isEmpty) {
            return
        }
        path!!.reset()
        invalidate()
    }
}