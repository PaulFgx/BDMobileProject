package fr.paulfgx.bdmobileproject.ui.utils

import android.util.TypedValue
import android.view.View
import kotlin.math.roundToInt

fun View.dp(number: Number): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        number.toFloat(),
        this.resources.displayMetrics
    ).roundToInt()
}