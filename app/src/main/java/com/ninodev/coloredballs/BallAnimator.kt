package com.ninodev.coloredballs

import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator

class BallAnimator(columnHeight: Float, height: Float, bottomMargin: Float) {
    val columnHeight = columnHeight
    val height  = height
    val bottomMargin = bottomMargin
    var positionSize = 0f
    var positionAdd = 0f
    init {
         this.positionSize = (this.columnHeight / 8)
         this.positionAdd = positionSize / 2
    }

    fun animateToPosition(
        ball: Ball, column: Column
    ) {

        val targetY = calculateTargetY(column)
        val animator = ObjectAnimator.ofFloat(ball, "y", ball.y, targetY)
        animator.duration = 500 // Duración de la animación en milisegundos
        animator.interpolator =
            AccelerateDecelerateInterpolator() // Interpolator para animación suave
        animator.start()
        ball.isFalling = true
    }

    private fun calculateTargetY(
        column: Column
    ): Float {
        if (GameView.FLAG_IN_COLUM) {
            GameView.FLAG_IN_COLUM = false
            return height - (positionSize * (column.balls.size - 1)) - positionAdd - bottomMargin
        } else {
            return height - (positionSize * column.balls.size) - positionAdd - bottomMargin
        }
    }
}
