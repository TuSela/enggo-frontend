package com.example.appenggo.view

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appenggo.R

class QuizResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_result)

        // ── Nhận dữ liệu từ Intent ──────────────────────────────────────────
        val correctCount   = intent.getIntExtra("CORRECT_COUNT", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val score          = intent.getDoubleExtra("SCORE", 0.0)
        val timeTaken      = intent.getStringExtra("TIME_TAKEN") ?: "00:00"
        val expGained      = intent.getIntExtra("EXP_GAINED", 0)

        // ── Tính toán ─────────────────────────────────────────────────────────
        val accuracy = if (totalQuestions > 0)
            (correctCount.toDouble() / totalQuestions * 100).toInt()
        else 0

        val levelProgress = intent.getIntExtra("LEVEL_PROGRESS", accuracy)

        // ── Ánh xạ View ───────────────────────────────────────────────────────
        val tvAccuracy    = findViewById<TextView>(R.id.tv_result_accuracy)
        val tvTime        = findViewById<TextView>(R.id.tv_result_time)
        val tvScore       = findViewById<TextView>(R.id.tv_result_score)
        val tvProgressPct = findViewById<TextView>(R.id.tv_progress_percent)
        val progressBar   = findViewById<ProgressBar>(R.id.progress_bar_level)
        val tvXpGained    = findViewById<TextView>(R.id.tv_xp_gained)
        val btnFinish     = findViewById<Button>(R.id.btn_finish)

        // ── Hiển thị ─────────────────────────────────────────────────────────
        tvAccuracy.text    = "$accuracy%"
        tvTime.text        = timeTaken
        tvScore.text       = String.format("%.1f", score)
        tvProgressPct.text = "$levelProgress%"
        progressBar.progress = levelProgress
        tvXpGained.text    = "+$expGained XP"

        // ── Nút ───────────────────────────────────────────────────────────────
        btnFinish.setOnClickListener { finish() }
        findViewById<android.widget.ImageButton?>(R.id.btn_back)?.setOnClickListener { finish() }
    }
}