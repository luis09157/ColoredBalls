package com.ninodev.coloredballs

import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator

object BallAnimator {

    fun animateToPosition(
        ball: Ball, column: Column, columnHeight: Float,
        height: Float, bottomMargin: Float
    ) {
        val targetY = calculateTargetY(column, columnHeight, height, bottomMargin)
        val animator = ObjectAnimator.ofFloat(ball, "y", ball.y, targetY)
        animator.duration = 500 // Duración de la animación en milisegundos
        animator.interpolator = AccelerateDecelerateInterpolator() // Interpolator para animación suave
        animator.start()
        ball.isFalling = true
    }

    private fun calculateTargetY(column: Column, columnHeight: Float, height: Float, bottomMargin: Float): Float {
        val positionSize = (columnHeight / column.maxBalls)
        val positionAdd = positionSize / 2
        return  height - (positionSize * column.balls.size) - positionAdd - bottomMargin
    }
}
