package com.example.appenggo.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.Resource
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.ExamAdapter
import com.example.appenggo.adapter.ThemeAdapter
import com.example.appenggo.repository.ThemeRepository
import com.example.appenggo.viewmodel.VocabularyViewModel
import com.example.appenggo.viewmodel.VocabularyViewModelFactory

class VocabularyActivity : AppCompatActivity() {

    private lateinit var viewModel: VocabularyViewModel
    private lateinit var themeAdapter: ThemeAdapter
    private lateinit var examAdapter: ExamAdapter
    
    private var selectedThemeId: Int? = null
    private var selectedDifficulty = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vocabulary)

        setupViewModel()
        initViews()
        setupObservers()
        
        val token = getToken()
        if (token != null) {
            viewModel.fetchThemes(token)
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPref.getString("TOKEN", null)
    }

    private fun setupViewModel() {
        val repository = ThemeRepository(RetrofitClient.api)
        val factory = VocabularyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[VocabularyViewModel::class.java]
    }

    private fun initViews() {
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnStart = findViewById<Button>(R.id.btn_start)
        val rvThemes = findViewById<RecyclerView>(R.id.rv_themes)
        val rvExams = findViewById<RecyclerView>(R.id.rv_exams)
        val tvResultsLabel = findViewById<TextView>(R.id.tv_results_label)

        val tvEasy = findViewById<TextView>(R.id.tv_easy)
        val tvMedium = findViewById<TextView>(R.id.tv_medium)
        val tvHard = findViewById<TextView>(R.id.tv_hard)

        // 1. Setup RecyclerView cho Chủ đề
        themeAdapter = ThemeAdapter(emptyList()) { selectedTheme ->
            selectedThemeId = selectedTheme.id
        }
        rvThemes.layoutManager = GridLayoutManager(this, 2)
        rvThemes.adapter = themeAdapter

        // 2. Setup RecyclerView cho Đề thi
        examAdapter = ExamAdapter(emptyList()) { selectedExam ->
            // Chuyển sang QuizActivity khi chọn đề
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("EXAM_ID", selectedExam.id)
            startActivity(intent)
        }
        rvExams.layoutManager = LinearLayoutManager(this)
        rvExams.adapter = examAdapter

        btnBack.setOnClickListener { finish() }

        // Logic chọn độ khó
        tvEasy.setOnClickListener { updateDifficultyUI(1, tvEasy, tvMedium, tvHard) }
        tvMedium.setOnClickListener { updateDifficultyUI(2, tvMedium, tvEasy, tvHard) }
        tvHard.setOnClickListener { updateDifficultyUI(3, tvHard, tvEasy, tvMedium) }

        updateDifficultyUI(1, tvEasy, tvMedium, tvHard)

        // Nút "Tìm đề thi"
        btnStart.setOnClickListener {
            val token = getToken()
            if (selectedThemeId == null) {
                Toast.makeText(this, "Hãy chọn một chủ đề trước!", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                viewModel.searchExams(token, selectedThemeId!!, selectedDifficulty)
            }
        }
    }

    private fun setupObservers() {
        // Quan sát danh sách chủ đề
        viewModel.themes.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { themeAdapter.updateData(it) }
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        // Quan sát kết quả tìm kiếm đề thi
        viewModel.exams.observe(this) { resource ->
            val rvExams = findViewById<RecyclerView>(R.id.rv_exams)
            val tvResultsLabel = findViewById<TextView>(R.id.tv_results_label)
            val btnStart = findViewById<Button>(R.id.btn_start)

            when (resource) {
                is Resource.Loading -> {
                    btnStart.isEnabled = false
                }
                is Resource.Success -> {
                    btnStart.isEnabled = true
                    val examsList = resource.data?.content
                    if (!examsList.isNullOrEmpty()) {
                        examAdapter.updateData(examsList)
                        tvResultsLabel.visibility = View.VISIBLE
                        rvExams.visibility = View.VISIBLE
                    } else {
                        tvResultsLabel.visibility = View.GONE
                        rvExams.visibility = View.GONE
                        Toast.makeText(this, "Không có đề thi nào phù hợp!", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    btnStart.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateDifficultyUI(diff: Int, selected: TextView, unselected1: TextView, unselected2: TextView) {
        selectedDifficulty = diff
        selected.setBackgroundResource(R.drawable.bg_button_orange_grad)
        selected.setTextColor(Color.WHITE)
        unselected1.setBackgroundResource(R.drawable.bg_chip_gray)
        unselected1.setTextColor(Color.BLACK)
        unselected2.setBackgroundResource(R.drawable.bg_chip_gray)
        unselected2.setTextColor(Color.BLACK)
    }
}