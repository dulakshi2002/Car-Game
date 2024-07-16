package com.example.nitrorush

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity(), GameTask {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var startBtn: Button
    private lateinit var speedUpBtn: Button
    private lateinit var speedDownBtn: Button
    private lateinit var mGameView: GameView
    private lateinit var scoreTextView: TextView
    private lateinit var playerNameTextView: TextView
    private lateinit var speedTextView: TextView
    private var highestScore: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //retrieving data from sharedPreference
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        startBtn = findViewById(R.id.startBtn)
        speedUpBtn = findViewById(R.id.speedup)
        speedDownBtn = findViewById(R.id.speeddown)
        rootLayout = findViewById(R.id.rootLayout)
        scoreTextView = findViewById(R.id.score)
        playerNameTextView = findViewById(R.id.userNameTextView)
        speedTextView = findViewById(R.id.speedText)

        // Retrieve the highest score from SharedPreferences
        highestScore = sharedPreferences.getInt("highestScore", 0)

        // Display the welcome message with placeholders
        val playerName = sharedPreferences.getString("playerName", "")
        val welcomeMessage = getString(R.string.welcome_message, playerName, highestScore)
        playerNameTextView.text = welcomeMessage

        startBtn.setOnClickListener { startGame() }
        speedUpBtn.setOnClickListener { increaseSpeed() }
        speedDownBtn.setOnClickListener { decreaseSpeed() }
    }

    private fun startGame() {
        mGameView = GameView(this, this)
        rootLayout.addView(mGameView)
        startBtn.visibility = View.GONE
        scoreTextView.visibility = View.GONE
        speedTextView.visibility = View.GONE
        mGameView.startAnimation()
    }

    override fun closeGame(score: Int) {
        // Update the highest score if the current score is higher
        if (score > highestScore) {
            highestScore = score
            // Save the highest score to SharedPreferences
            sharedPreferences.edit().putInt("highestScore", highestScore).apply()
        }

        // Update the UI with the current score
        scoreTextView.text = "Score: $score"

        // Remove the GameView from the layout and stop animation
        rootLayout.removeView(mGameView)
        mGameView.stopAnimation()

        // Show start button, score text view, and speed text view again
        startBtn.visibility = View.VISIBLE
        scoreTextView.visibility = View.VISIBLE
        speedTextView.visibility = View.VISIBLE
    }

    private fun increaseSpeed() {
        mGameView.increaseSpeed()
        updateSpeedTextView()
    }

    private fun decreaseSpeed() {
        mGameView.decreaseSpeed()
        updateSpeedTextView()
    }

    private fun updateSpeedTextView() {
        speedTextView.text = "Speed: ${mGameView.speed}"
    }
}

