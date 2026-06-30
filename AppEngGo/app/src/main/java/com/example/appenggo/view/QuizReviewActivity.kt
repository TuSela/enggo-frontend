package com.example.appenggo.view

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.ReviewAdapter
import com.example.appenggo.model.ReviewResponse
import com.example.appenggo.repository.ThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuizReviewActivity : AppCompatActivity() {

    private lateinit var rvQuestions: RecyclerView
    private lateinit var tvExamTitle: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvScore: TextView
    private lateinit var btnBack: ImageView

    private val repository = ThemeRepository(RetrofitClient.api)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_review)

        initViews()
        val attemptId = intent.getIntExtra("ATTEMPT_ID", -1)
        if (attemptId != -1) {
            fetchReviewData(attemptId)
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin bài làm", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        rvQuestions = findViewById(R.id.rv_review_questions)
        tvExamTitle = findViewById(R.id.tv_review_exam_title)
        tvAccuracy  = findViewById(R.id.tv_review_accuracy)
        tvTime      = findViewById(R.id.tv_review_time)
        tvScore     = findViewById(R.id.tv_review_score)
        btnBack     = findViewById(R.id.btn_back)

        rvQuestions.layoutManager = LinearLayoutManager(this)
        btnBack.setOnClickListener { finish() }
    }

    private fun fetchReviewData(attemptId: Int) {
        val token = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("TOKEN", null)
        if (token == null) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.reviewAttempt(token, attemptId)
                }
                if (response.code == 1000 && response.result != null) {
                    displayReview(response.result)
                } else {
                    Toast.makeText(this@QuizReviewActivity, response.message ?: "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@QuizReviewActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayReview(review: ReviewResponse) {
        tvExamTitle.text = review.examTitle
        tvAccuracy.text  = "${review.accuracyPercent.toInt()}%"
        tvTime.text      = review.timeSpent
        tvScore.text     = String.format("%.1f", review.totalScore)

        rvQuestions.adapter = ReviewAdapter(review.questions)
    }
}
