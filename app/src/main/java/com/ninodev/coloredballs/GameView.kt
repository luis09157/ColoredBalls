package com.ninodev.coloredballs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val TAG = "GameViewTAG"
    private val paint = Paint()

    // Listas para almacenar bolas y columnas
    private val balls = mutableListOf<Ball>()
    private val columns = mutableListOf<Column>()

    // Variable para la bola que se está arrastrando
    private var draggingBall: Ball? = null

    // Variables para dimensiones escaladas
    private var columnWidth = 0f
    private var columnHeight = 0f
    private var bottomMargin = 0f
    private var ballRadius = 0f

    // ID de la bola seleccionada
    companion object {
        var ID_BALL_SELECTED = 0
    }

    init {
        initializeColumns()
        initializeBalls()
    }

    // Inicialización de columnas
    private fun initializeColumns() {
        for (i in 0 until 6) {
            columns.add(Column(i))
        }
    }

    // Inicialización de bolas con colores y posiciones
    // Inicialización de bolas con colores y posiciones
    private fun initializeBalls() {
        // Lista de colores en orden aleatorio pero consistente
        val colors = listOf(Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE, Color.MAGENTA)
            .shuffled(Random(123)) // El número dentro de Random() determina la semilla

        var ballId = 0

        for (y in colors.indices) {
            val initialColumn = columns[y]
            for (i in 0 until 8) {
                val ball = Ball(colors[y], 0f, 0f, ballId++) // Asignar ID único
                balls.add(ball)
                initialColumn.addBall(ball)
            }
        }
    }


    // Escalado de objetos según las dimensiones de la vista
    private fun scaleObjects() {
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()

        // Calcular dimensiones y posiciones
        columnWidth = screenWidth / 7
        columnHeight = screenHeight * 0.8f
        bottomMargin = screenHeight * 0.1f
        ballRadius = columnWidth * 0.3f

        // Posicionar bolas inicialmente
        for (column in columns) {
            val left = (column.id + 0.5f) * columnWidth
            val bottom = screenHeight - bottomMargin
            for (i in column.balls.indices) {
                val ball = column.balls[i]
                if (ball.id != ID_BALL_SELECTED) {
                    ball.x = left + columnWidth * 0.4f
                    ball.y = bottom - (i + 0.5f) * (columnHeight / column.maxBalls)
                }
            }
        }
    }

    // Dibujar las columnas en la vista
    private fun drawColumns(canvas: Canvas) {
        for (i in 0 until 6) {
            val left = (i + 0.5f) * columnWidth
            val top = height - bottomMargin - columnHeight
            val right = left + columnWidth * 0.8f
            val bottom = height - bottomMargin

            paint.color = Color.LTGRAY
            canvas.drawRect(left, top, right, bottom, paint)
        }
    }

    // Dibujar las bolas en la vista
    private fun drawBalls(canvas: Canvas) {
        for (column in columns) {
            for (ball in column.balls) {
                paint.color = ball.color
                canvas.drawCircle(ball.x, ball.y, ballRadius, paint)
            }
        }
    }

    // Método para obtener la bola tocada en la posición actual
    private fun getTouchedBall(touchX: Float, touchY: Float): Ball? {
        for (column in columns) {
            val lastBall = column.balls.lastOrNull()
            if (lastBall != null && isTouchingCircle(touchX, touchY, lastBall.x, lastBall.y, ballRadius)) {
                Log.e(TAG, "Touched the last ball in column with ID: ${column.id}")
                return lastBall
            }
        }
        return null
    }

    // Verificar si se ha tocado una bola en forma circular
    private fun isTouchingCircle(touchX: Float, touchY: Float, circleX: Float, circleY: Float, radius: Float): Boolean {
        val dx = touchX - circleX
        val dy = touchY - circleY
        return dx * dx + dy * dy <= radius * radius
    }

    // Manejar eventos táctiles en la vista
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleTouchDown(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                handleTouchMove(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP -> {
                handleTouchUp(event.x)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // Procesar acción de toque hacia abajo
    private fun handleTouchDown(touchX: Float, touchY: Float) {
        draggingBall = getTouchedBall(touchX, touchY)
        draggingBall?.let {
            ID_BALL_SELECTED = it.id
            Log.d(TAG, "Selected ball with ID: ${it.color}")
        }

        invalidate()
    }

    // Procesar movimiento de toque
    private fun handleTouchMove(touchX: Float, touchY: Float) {
        draggingBall?.let {
            it.x = touchX
            it.y = touchY
        }

        invalidate()
    }

    // Procesar acción de toque hacia arriba
    private fun handleTouchUp(dropX: Float) {
        draggingBall?.let { ball ->
            ball.isBeingDragged = false
            handleBallDrop(ball, dropX)
            draggingBall = null
            invalidate()
        }
    }

    // Manejar la soltura de la bola en la columna más cercana
    private fun handleBallDrop(ball: Ball, dropX: Float) {
        // Encontrar la columna más cercana para soltar la bola
        val targetColumn = columns.minByOrNull { column ->
            Math.abs((column.id + 0.5f) * columnWidth - dropX)
        }

        targetColumn?.let {
            // Verificar si la columna puede aceptar más bolas
            if (it.canAddBall()) {
                // Remover la bola de la columna anterior y agregarla a la nueva
                columns.forEach { column -> column.removeBall(ball) }
                it.addBall(ball)
                // Recalcular la posición de la bola en la nueva columna
                val left = (it.id + 0.5f) * columnWidth
                val bottom = height - bottomMargin
                val newBallIndex = it.balls.indexOf(ball)
                ball.x = left + columnWidth * 0.4f
                ball.y = bottom - (newBallIndex + 0.5f) * (columnHeight / it.maxBalls)
            } else {
                // Si no se puede añadir a la nueva columna, devolver la bola a su posición original
                val originalColumn = columns.find { column -> column.balls.contains(ball) }
                originalColumn?.let {
                    val left = (it.id + 0.5f) * columnWidth
                    val bottom = height - bottomMargin
                    val originalBallIndex = it.balls.indexOf(ball)
                    ball.x = left + columnWidth * 0.4f
                    ball.y = bottom - (originalBallIndex + 0.5f) * (columnHeight / it.maxBalls)
                }
            }
        }
    }

    // Dibujar en la vista
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        scaleObjects()
        drawColumns(canvas)
        drawBalls(canvas)
    }
}
