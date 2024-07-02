package com.ninodev.coloredballs

class Column(val id: Int, val balls: MutableList<Ball> = mutableListOf()) {
    val maxBalls = 8

    fun addBall(ball: Ball): Boolean {
        if (balls.size < maxBalls) {

            balls.add( balls.size, ball) // Agregar la bola al inicio de la lista
            return true
        }
        return false
    }



    fun removeBall(ball: Ball) {
        balls.remove(ball)
    }
    // Dentro de la clase Column
    fun isCompleteWithSameColor(): Boolean {
        if (balls.size < maxBalls) return false

        val firstColor = balls.firstOrNull()?.color ?: return false
        return balls.all { it.color == firstColor }
    }


    fun canAddBall(): Boolean {
        return balls.size < maxBalls
    }
}
