package com.example.appenggo.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appenggo.R

class QuizResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_result)

        // Nhận dữ liệu từ intent
        val correctCount = intent.getIntExtra("CORRECT_COUNT", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val score = intent.getDoubleExtra("SCORE", 0.0)
        val timeTaken = intent.getStringExtra("TIME_TAKEN") ?: "00:00"

        // Tính toán độ chính xác (%)
        val accuracy = if (totalQuestions > 0) (correctCount.toDouble() / totalQuestions * 100).toInt() else 0

        // Ánh xạ và hiển thị dữ liệu lên giao diện
        findViewById<TextView>(R.id.tv_result_score).text = String.format("%.1f", score)
        findViewById<TextView>(R.id.tv_result_accuracy).text = "$accuracy%"
        findViewById<TextView>(R.id.tv_result_time).text = timeTaken
        
        findViewById<TextView>(R.id.tv_correct_summary).text = 
            "Bạn đã trả lời đúng $correctCount trên tổng số $totalQuestions câu!"

        // Nút hoàn tất quay về màn hình trước đó
        findViewById<Button>(R.id.btn_finish).setOnClickListener {
            finish()
        }
    }
}
