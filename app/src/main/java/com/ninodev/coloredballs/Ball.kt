package com.ninodev.coloredballs

data class Ball(var color: Int, var x: Float, var y: Float, val id: Int, var isBeingDragged: Boolean = false)
