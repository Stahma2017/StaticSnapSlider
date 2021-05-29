



package ru.myapplication.sdslidersample.ui.custom2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import ru.myapplication.sdslidersample.R
import ru.myapplication.sdslidersample.dpToPx

class SnapSlider @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet) : View(context, attrs) {

    @ColorInt
    private val trackColor = context.getColor(R.color.purple_500)
    private val thumbColor = context.getColor(R.color.teal_200)

    private val trackWidth = 20f.dpToPx()
    private val trackRadius = 10f.dpToPx()
    private val thumbRadius = 10f.dpToPx()


    private val trackPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.BUTT
        color = trackColor
    }

    private val thumbPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.BUTT
        color = thumbColor
    }

    override fun onDraw(canvas: Canvas) {
        drawTrack(canvas)
        drawThumb(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)

    }

    private fun drawThumb(canvas: Canvas) {
        canvas.drawCircle(paddingLeft.toFloat() + thumbRadius, paddingTop + (trackWidth/2), thumbRadius, thumbPaint)
    }

    private fun drawTrack(canvas: Canvas) {
        canvas.drawRoundRect(paddingLeft.toFloat(), paddingTop.toFloat(), measuredWidth - paddingRight.toFloat(), paddingTop + trackWidth, trackRadius, trackRadius, trackPaint)
    }
}