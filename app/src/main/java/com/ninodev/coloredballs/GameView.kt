package com.ninodev.coloredballs

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val TAG = "GameViewTAG"
    private val paint = Paint()

    // Listas para almacenar bolas y columnas
    private val balls = mutableListOf<Ball>()
    private val columns = mutableListOf<Column>()

    private var ballAnimator : BallAnimator? = null

    // Variable para la bola que se está arrastrando
    private var draggingBall: Ball? = null
    private var touchOffset = PointF(0f, 0f)

    // Variables para dimensiones escaladas
    private var columnWidth = 0f
    private var columnHeight = 0f
    private var bottomMargin = 0f
    private var ballRadius = 0f
    private var leftPositionBall = 0.6f

    // Variables de física
    private val gravity = 0.5f // Gravedad

    // ID de la bola seleccionada
    companion object {
        var ID_BALL_SELECTED = -1
        var FLAG_IN_COLUM = false
    }

    init {
        initializeColumns()
        initializeBalls()
    }
    fun initBallAnimator(){
        ballAnimator = BallAnimator(columnHeight, height.toFloat(),bottomMargin)
    }

    // Inicialización de columnas
    private fun initializeColumns() {
        for (i in 0 until 6) {
            columns.add(Column(i))
        }
    }

    // Inicialización de bolas con colores y posiciones
    private fun initializeBalls() {
        val colorsAndTextures = listOf(
            "Azul" to R.drawable.bola_azul,
            "Roja" to R.drawable.bola_roja,
            "Rosa" to R.drawable.bola_rosa,
            "Amarillo" to R.drawable.bola_amarilla,
            "Verde" to R.drawable.bola_verde
        )

        // Load bitmaps from resources
        val textures = colorsAndTextures.map { (colorName, resId) ->
            BitmapFactory.decodeResource(resources, resId)
        }

        var ballId = 0

        for (textureIndex in textures.indices) {
            val texture = textures[textureIndex]
            repeat(8) {
                val ball = Ball(texture, 0f, 0f, ballId++, colorsAndTextures[textureIndex].first) // Asignar ID único y color
                balls.add(ball)
            }
        }

        // Shuffle balls randomly but consistently
        balls.shuffle(Random(12345))

        // Distribute balls into columns
        for (i in balls.indices) {
            val column = columns[i % 5] // Distribuir en las primeras 5 columnas
            column.addBall(balls[i])
        }
    }
    // Escalado de objetos según las dimensiones de la vista
    private fun scaleObjects() {
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()

        // Calcular dimensiones y posiciones
        columnWidth = screenWidth / 7
        columnHeight = screenHeight * 0.7f
        bottomMargin = screenHeight * 0.03f
        ballRadius = columnWidth * leftPositionBall

        // Posicionar bolas inicialmente
        for (column in columns) {
            for (i in column.balls.indices) {
                val ball = column.balls[i]
                if (ball.id != ID_BALL_SELECTED) {
                    updateBallPosition(ball, column)
                }
            }
        }
        initBallAnimator()
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
                if (ball.isBeingDragged && ball == draggingBall) {
                    // Dibujar la bola arrastrada en la posición del touch
                    canvas.drawBitmap(ball.texture, ball.x - ballRadius, ball.y - ballRadius, paint)
                } else {
                    // Dibujar las otras bolas en sus posiciones originales
                    canvas.drawBitmap(ball.texture, ball.x - ballRadius, ball.y - ballRadius, paint)
                }
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
        draggingBall?.let { ball ->
            ID_BALL_SELECTED = ball.id
            touchOffset.x = ball.x - touchX
            touchOffset.y = ball.y - touchY
        }
        invalidate()
    }

    // Procesar movimiento de toque
    private fun handleTouchMove(touchX: Float, touchY: Float) {
        draggingBall?.let {
            it.x = touchX + touchOffset.x
            it.y = touchY + touchOffset.y
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

    private fun updateBallPosition(ball: Ball, column: Column) {
        val positionSize = columnHeight / column.maxBalls
        val positionAdd = positionSize / 2
        val ballIndex = column.balls.indexOf(ball)
        val bottom = height - bottomMargin

        ball.x = (column.id + leftPositionBall) * columnWidth + columnWidth * 0.5f
        ball.y = bottom - (ballIndex + 0.5f) * positionSize
    }


    // Manejar la soltura de la bola en la columna más cercana
    private fun handleBallDrop(ball: Ball, dropX: Float) {
        // Encontrar la columna más cercana para soltar la bola
        val targetColumn = columns.minByOrNull { column ->
            abs((column.id + leftPositionBall) * columnWidth - dropX)
        }

        // Mantener una referencia a la columna original y la posición inicial
        val originalColumn = columns.find { it.balls.contains(ball) }
        //val originalIndex = originalColumn?.balls?.indexOf(ball) ?: -1
        val originalX = ball.x
        val originalY = ball.y

        // Verificar si la bola se soltó en la misma columna
        if (originalColumn != null && originalColumn.id == targetColumn?.id) {
            // Si está en la misma columna, solo actualizar su posición
            ball.x = originalX
            ball.y = originalY

            FLAG_IN_COLUM = true
            // Animar la bola hacia la posición original en la columna
            startFallingAnimation(ball,originalColumn)
            // Recalcular la posición de la bola en la columna original
            updateBallPosition(ball, originalColumn)


        } else {
            // Si no está en la misma columna, mover la bola a la columna objetivo si es posible
            targetColumn?.let { column ->
                if (column.canAddBall()) {
                    // Animar la bola hacia la posición final en la columna
                    startFallingAnimation(ball,column)
                    // Remover la bola de la columna anterior y agregarla a la nueva
                    originalColumn?.removeBall(ball)
                    column.addBall(ball)
                    // Recalcular la posición de la bola en la nueva columna
                    updateBallPosition(ball, column)
                } else {
                    // Si no se puede añadir a la nueva columna, devolver la bola a su posición original
                    ball.x = originalX
                    ball.y = originalY
                    // Animar la bola hacia la posición original en la columna
                    originalColumn?.let {
                        startFallingAnimation(ball, column)
                        // Actualizar su posición en la columna original
                        updateBallPosition(ball, it)
                    }
                    showToast("¡La columna está llena! La bola se ha devuelto a su posición original.")
                }
            }
        }

        // Verificar si la columna está completa con bolas del mismo color
        targetColumn?.let { column ->
            if (column.isCompleteWithSameColor()) {
                showToast("¡Columna completa con el color: ${ball.color}!")
            }
        }

        draggingBall = null
        invalidate()
    }




    // Dibujar en la vista
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        scaleObjects()
        applyPhysics()
        drawColumns(canvas)
        drawBalls(canvas)
        invalidate() // Volver a dibujar continuamente para actualizar físicas
    }

    // Aplicar físicas simples a las bolas
    private fun applyPhysics() {
        for (ball in balls) {
            if (ball.id != ID_BALL_SELECTED) {
                ball.y += gravity // Aplicar gravedad
                // Limitar para que no salga de la pantalla o de las columnas
                ball.y = min(ball.y, height - bottomMargin - ballRadius)
                ball.y = max(ball.y, ballRadius)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun startFallingAnimation(ball: Ball,column: Column) {
        ballAnimator?.animateToPosition(ball, column)
    }
}
