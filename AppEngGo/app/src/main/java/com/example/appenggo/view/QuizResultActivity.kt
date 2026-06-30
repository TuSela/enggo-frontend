package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.view.View
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
        val attemptId      = intent.getIntExtra("ATTEMPT_ID", -1)
        val correctCount   = intent.getIntExtra("CORRECT_COUNT", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val score          = intent.getDoubleExtra("SCORE", 0.0)
        val timeTaken      = intent.getStringExtra("TIME_TAKEN") ?: "00:00"
        val expGained      = intent.getIntExtra("EXP_GAINED", 0)
        val bonusExp       = intent.getIntExtra("BONUS_EXP", 0)

        // LevelInfo từ server (có thể null nếu backend chưa trả)
        val levelCurrent     = intent.getIntExtra("LEVEL_CURRENT", -1)
        val levelNext        = intent.getIntExtra("LEVEL_NEXT", -1)
        val expInLevel       = intent.getIntExtra("EXP_IN_LEVEL", -1)
        val expRequired      = intent.getIntExtra("EXP_REQUIRED", -1)
        val levelProgressPct = intent.getFloatExtra("LEVEL_PROGRESS_PCT", -1f)

        val hasLevelInfo = levelCurrent != -1 && levelProgressPct >= 0f

        // ── DEBUG: xem backend có trả levelInfo không ─────────────────────
        android.util.Log.d("QuizResult", "=== attemptId=$attemptId, levelCurrent=$levelCurrent, levelNext=$levelNext, pct=$levelProgressPct, expIn=$expInLevel, expReq=$expRequired, hasLevelInfo=$hasLevelInfo ===")

        // ── Tính accuracy ────────────────────────────────────────────────────
        val accuracy = if (totalQuestions > 0)
            (correctCount.toDouble() / totalQuestions * 100).toInt()
        else 0

        // ── Ánh xạ View ───────────────────────────────────────────────────────
        val tvAccuracy    = findViewById<TextView>(R.id.tv_result_accuracy)
        val tvTime        = findViewById<TextView>(R.id.tv_result_time)
        val tvScore       = findViewById<TextView>(R.id.tv_result_score)
        val tvProgressPct = findViewById<TextView>(R.id.tv_progress_percent)
        val progressBar   = findViewById<ProgressBar>(R.id.progress_bar_level)
        val tvXpGained    = findViewById<TextView>(R.id.tv_xp_gained)
        val tvLevelLabel  = findViewById<TextView>(R.id.tv_level_label)
        val btnReview     = findViewById<Button>(R.id.btn_review)
        val btnFinish     = findViewById<Button>(R.id.btn_finish)

        // ── Hiển thị stats ────────────────────────────────────────────────────
        tvAccuracy.text = "$accuracy%"
        tvTime.text     = timeTaken
        tvScore.text    = String.format("%.1f", score)

        // ── XP: hiển thị tổng nếu có bonus ───────────────────────────────────
        val totalXp = expGained + bonusExp
        tvXpGained.text = if (bonusExp > 0) "+$totalXp XP (+$bonusExp bonus)" else "+$expGained XP"

        // ── Level Progress bar ────────────────────────────────────────────────
        if (hasLevelInfo) {
            val pct = levelProgressPct.toInt().coerceIn(0, 100)
            progressBar.progress = pct
            tvProgressPct.text   = "$pct%"
            // "LV 5 · 350/700 XP"
            tvLevelLabel.visibility = View.VISIBLE
            tvLevelLabel.text = if (expInLevel >= 0 && expRequired > 0)
                "LV $levelCurrent · $expInLevel/$expRequired XP"
            else
                "LV $levelCurrent → LV $levelNext"
        } else {
            // Fallback khi backend chưa trả levelInfo
            progressBar.progress  = accuracy
            tvProgressPct.text    = "$accuracy%"
            tvLevelLabel.visibility = View.GONE
        }

        // ── Nút ───────────────────────────────────────────────────────────────
        val goHome = {
            val i = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("RELOAD_HOME", true)
            }
            startActivity(i)
            finish()
        }

        btnReview.setOnClickListener {
            if (attemptId != -1) {
                val intent = Intent(this, QuizReviewActivity::class.java)
                intent.putExtra("ATTEMPT_ID", attemptId)
                startActivity(intent)
            }
        }

        btnFinish.setOnClickListener { goHome() }
        findViewById<android.widget.ImageButton?>(R.id.btn_back)?.setOnClickListener { goHome() }
    }
}
