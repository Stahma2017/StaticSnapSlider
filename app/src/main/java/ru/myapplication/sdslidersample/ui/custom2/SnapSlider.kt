



package ru.myapplication.sdslidersample.ui.custom2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import ru.myapplication.sdslidersample.R
import ru.myapplication.sdslidersample.dpToPx
import ru.myapplication.sdslidersample.ui.custom.SizeUtils

class SnapSlider @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet) : View(context, attrs) {

    @ColorInt
    private val trackColor = context.getColor(R.color.purple_500)

    private val trackWidth = 20f.dpToPx()
    private val radius = 6f.dpToPx()


    private val trackPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.BUTT
        color = trackColor
        strokeWidth = trackWidth.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        drawTrack(canvas)
        drawThumb(canvas)
    }

    private fun drawThumb(canvas: Canvas) {

    }

    private fun drawTrack(canvas: Canvas) {
        canvas.drawRoundRect(paddingLeft.toFloat(), paddingTop.toFloat(), paddingRight.toFloat(), paddingTop + trackWidth, radius, radius, trackPaint)
    }
}