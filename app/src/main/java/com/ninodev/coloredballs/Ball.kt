package com.ninodev.coloredballs

import android.graphics.Bitmap

data class Ball(var texture: Bitmap, var x: Float, var y: Float, val id: Int, var isBeingDragged: Boolean = false)
