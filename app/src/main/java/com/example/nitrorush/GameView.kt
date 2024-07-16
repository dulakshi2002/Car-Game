package com.example.nitrorush

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random
import kotlin.time.times

class GameView(var c: Context, private var gameTask: GameTask) : View(c) {
    private var myPaint: Paint = Paint()
    var speed = 1
    private var time = 0
    private var score = 0
    private var myCar = 1 // Initialize mycar in the middle lane
    private val otherMonst = ArrayList<HashMap<String, Any>>()
    private var isRunning = false
    private var speedUpInterval = 10000L // 10 seconds initially
    private var lastSpeedUpTime = 0L

    private var viewWidth = 0
    private var viewHeight = 0

    // Drawables for myCar, otherCars, and road
    private lateinit var carDrawable: Drawable
    private lateinit var otherCarDrawables: Array<Drawable>
    private lateinit var roadDrawable: BitmapDrawable

    // Touch event variables
    private var previousX = 0f
    private var previousY = 0f
    private var isDragging = false

    init {
        myPaint = Paint()
        loadDrawables()
    }

    @SuppressLint("ResourceType")
    private fun loadDrawables() {
        carDrawable = context.getDrawable(R.drawable.car)!!
        otherCarDrawables = arrayOf(

            context.getDrawable(R.drawable.c2)!!,
            context.getDrawable(R.drawable.c3)!!,
            context.getDrawable(R.drawable.c4)!!,
            context.getDrawable(R.drawable.c5)!!
        )
        val roadDrawableRes = context.resources.openRawResource(R.drawable.road)
        val roadBitmap = BitmapFactory.decodeStream(roadDrawableRes)
        roadDrawable = BitmapDrawable(context.resources, roadBitmap)
    }

    //update score and speed
    private fun updateScoreAndSpeed() {
        score++
        speed = 1 + score / 8
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setBackgroundDrawable(roadDrawable)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (canvas == null) return

        viewWidth = width
        viewHeight = height

        // Draw lanes
        val laneWidth = viewWidth / 3
        myPaint.color = Color.WHITE
        myPaint.strokeWidth = 5f
        canvas.drawLine(laneWidth.toFloat(), 0f, laneWidth.toFloat(), viewHeight.toFloat(), myPaint)
        canvas.drawLine((laneWidth * 2).toFloat(), 0f, (laneWidth * 2).toFloat(), viewHeight.toFloat(), myPaint)

        // Draw center line
        val centerX = laneWidth.toFloat() + laneWidth / 2
        myPaint.color = Color.YELLOW
        for (i in 0 until viewHeight step 50) {
            canvas.drawLine(centerX, i.toFloat(), centerX, (i + 30).toFloat(), myPaint)
        }

        // Update time and  cars
        if (isRunning) {
            val currentTime = System.currentTimeMillis()
            time += 10 + speed
            if (time % 700 < 10 + speed) {
                val map = HashMap<String, Any>()
                map["lane"] = (0..2).random()
                map["startTime"] = time
                map["monsterDrawable"] = otherCarDrawables.random() // Randomly select a cars Drawable
                otherMonst.add(map)
            }
        }

        // Draw the myCar
        val carWidth = laneWidth / 2 // adjust according to your design
        val carHeight = carWidth * 1 // adjust according to your design
        val carX = myCar * laneWidth + (laneWidth - carWidth) / 2
        val carY = viewHeight - carHeight
        carDrawable.setBounds(carX, carY, carX + carWidth, carY + carHeight)
        carDrawable.draw(canvas)

        // Draw other cars and handle collisions
        val indicesToRemove = mutableListOf<Int>()
        for (i in otherMonst.indices) {
            val otherCarX = otherMonst[i]["lane"] as Int * laneWidth + (laneWidth - carWidth) / 2
            var otherCarY = time - (otherMonst[i]["startTime"] as Int)

            if (otherCarY < -carHeight) {
                indicesToRemove.add(i)
                updateScoreAndSpeed()
            } else {
                val monsterDrawable = otherMonst[i]["monsterDrawable"] as Drawable
                val monsterWidth = laneWidth / 2 // adjust according to your design
                val monsterHeight = monsterWidth * 1 // adjust according to your design
                monsterDrawable.setBounds(otherCarX, otherCarY.toInt(), otherCarX + monsterWidth, (otherCarY + monsterHeight).toInt())
                monsterDrawable.draw(canvas)
                if (isRunning && otherMonst[i]["lane"] as Int == myCar && otherCarY > carY - monsterHeight && otherCarY < carY + carHeight) {
                    // Game over if a collision occurs
                    gameTask.closeGame(score)
                } else if (isRunning && otherCarY >= carY + carHeight && otherCarY < carY + 2 * carHeight) {
                    // Score increases when the myCar successfully dodges a cars
                    score += 5
                }
            }
        }

        // Remove cars outside the loop
        indicesToRemove.forEach { index ->
            otherMonst.removeAt(index)
        }

        // Draw score and speed
        myPaint.color = Color.WHITE
        myPaint.textSize = 40f
        canvas.drawText("Score : $score", 80f, 80f, myPaint)
        canvas.drawText("Speed : $speed", 380f, 80f, myPaint)

        // Redraw the view
        if (isRunning) {
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val laneWidth = viewWidth / 3
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                previousX = x
                previousY = y
                isDragging = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val dx = x - previousX
                    val dy = y - previousY

                    // Update the car position based on the drag movement
                    val newLane = when {
                        x < laneWidth -> 0
                        x < laneWidth * 2 -> 1
                        else -> 2
                    }
                    myCar = newLane

                    previousX = x
                    previousY = y
                }
            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
            }
        }
        return true
    }

    //start animation loop
    fun startAnimation() {
        isRunning = true
        invalidate()
    }

    //stop animation loop
    fun stopAnimation() {
        isRunning = false
    }

    fun increaseSpeed() {
        // Increase the speed by a certain factor
        speed++
    }

    fun decreaseSpeed() {
        // Decrease the speed by a certain factor
        if (speed > 1) {
            speed--
        }
    }
}
