package com.example.kaskelasapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class SpotlightView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eraser = Paint(Paint.ANTI_ALIAS_FLAG)
    private var targetRect = RectF()
    private var isVisible = false

    init {
        paint.color = Color.parseColor("#CC000000") // 80% black
        eraser.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setTarget(view: View?): Float {
        if (view == null) {
            isVisible = false
            return 0f
        } else {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            
            val viewLocation = IntArray(2)
            getLocationOnScreen(viewLocation)
            
            val relativeX = (location[0] - viewLocation[0]).toFloat()
            val relativeY = (location[1] - viewLocation[1]).toFloat()

            targetRect.set(
                relativeX - 30,
                relativeY - 30,
                relativeX + view.width + 30,
                relativeY + view.height + 30
            )
            isVisible = true
            invalidate()
            return relativeY + (view.height / 2)
        }
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isVisible) return
        
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        canvas.drawRoundRect(targetRect, 20f, 20f, eraser)
        
        // Border around hole
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.color = Color.WHITE
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 4f
        canvas.drawRoundRect(targetRect, 20f, 20f, borderPaint)
    }
}

