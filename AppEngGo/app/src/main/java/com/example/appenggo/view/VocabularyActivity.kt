package com.example.appenggo.view

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appenggo.R

class VocabularyActivity : AppCompatActivity() {

    private var questionCount = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vocabulary)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnMinus = findViewById<TextView>(R.id.btn_minus)
        val btnPlus = findViewById<TextView>(R.id.btn_plus)
        val tvQuestionCount = findViewById<TextView>(R.id.tv_question_count)
        val btnStart = findViewById<TextView>(R.id.btn_start)

        btnBack.setOnClickListener {
            finish()
        }

        btnMinus.setOnClickListener {
            if (questionCount > 5) {
                questionCount -= 5
                tvQuestionCount.text = questionCount.toString()
            }
        }

        btnPlus.setOnClickListener {
            if (questionCount < 50) {
                questionCount += 5
                tvQuestionCount.text = questionCount.toString()
            }
        }

        btnStart.setOnClickListener {
            // Logic bắt đầu luyện tập sẽ thêm sau
        }
    }
}