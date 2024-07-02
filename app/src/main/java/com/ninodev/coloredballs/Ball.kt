package com.ninodev.coloredballs

import android.graphics.Bitmap

data class Ball(
    var texture: Bitmap,
    var x: Float,
    var y: Float,
    val id: Int,
    val color: String, // Nueva variable para el color
    var isBeingDragged: Boolean = false
) {

    var isFalling: Boolean = false


}
