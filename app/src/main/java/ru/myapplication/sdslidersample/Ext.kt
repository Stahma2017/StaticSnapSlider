package ru.myapplication.sdslidersample

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

fun Context.attrValue(res: Int): Int {
    val value: Int
    val tv = TypedValue()
    if (this.theme.resolveAttribute(res, tv, true)) value = tv.data
    else throw Resources.NotFoundException("Resource with id $res not found")
    return value
}

fun Context.dpToIntPx(dp : Int) : Int {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            this.resources.displayMetrics).toInt()
}

fun Int.pxToDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Float.pxToDp(): Float = (this / Resources.getSystem().displayMetrics.density)
fun Float.dpToPx(): Float = (this * Resources.getSystem().displayMetrics.density)
