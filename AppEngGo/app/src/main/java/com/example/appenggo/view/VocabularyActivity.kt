package com.example.appenggo.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.Resource
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.ThemeAdapter
import com.example.appenggo.repository.ThemeRepository
import com.example.appenggo.viewmodel.VocabularyViewModel
import com.example.appenggo.viewmodel.VocabularyViewModelFactory
import com.google.gson.Gson

class VocabularyActivity : AppCompatActivity() {

    private lateinit var viewModel: VocabularyViewModel
    private lateinit var themeAdapter: ThemeAdapter

    private var selectedThemeId: Int? = null
    private var selectedDifficulty = 1
    private var selectedCount = 10
    private val minCount = 5
    private val maxCount = 20
    private val stepCount = 5
    private val defaultQuestionTypes = listOf("MULTIPLE_CHOICE", "FILL_BLANK", "MATCHING")
    private var isNavigating = false  // flag tránh navigate 2 lần

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

    override fun onResume() {
        super.onResume()
        isNavigating = false  // reset khi quay lại từ QuizActivity
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
        val btnBack      = findViewById<ImageView>(R.id.btn_back)
        val btnStart     = findViewById<Button>(R.id.btn_start)
        val rvThemes     = findViewById<RecyclerView>(R.id.rv_themes)
        val tvEasy       = findViewById<TextView>(R.id.tv_easy)
        val tvMedium     = findViewById<TextView>(R.id.tv_medium)
        val tvHard       = findViewById<TextView>(R.id.tv_hard)
        val btnMinus     = findViewById<TextView>(R.id.btn_count_minus)
        val btnPlus      = findViewById<TextView>(R.id.btn_count_plus)
        val tvCountValue = findViewById<TextView>(R.id.tv_count_value)

        themeAdapter = ThemeAdapter(emptyList()) { selectedTheme ->
            selectedThemeId = selectedTheme.id
        }
        rvThemes.layoutManager = GridLayoutManager(this, 2)
        rvThemes.adapter = themeAdapter

        btnBack.setOnClickListener { finish() }

        tvEasy.setOnClickListener   { updateDifficultyUI(1, tvEasy, tvMedium, tvHard) }
        tvMedium.setOnClickListener { updateDifficultyUI(2, tvMedium, tvEasy, tvHard) }
        tvHard.setOnClickListener   { updateDifficultyUI(3, tvHard, tvEasy, tvMedium) }
        updateDifficultyUI(1, tvEasy, tvMedium, tvHard)

        updateCountDisplay(tvCountValue, btnMinus, btnPlus)

        btnMinus.setOnClickListener {
            if (selectedCount > minCount) {
                selectedCount -= stepCount
                updateCountDisplay(tvCountValue, btnMinus, btnPlus)
            }
        }
        btnPlus.setOnClickListener {
            if (selectedCount < maxCount) {
                selectedCount += stepCount
                updateCountDisplay(tvCountValue, btnMinus, btnPlus)
            }
        }

        btnStart.setOnClickListener {
            val token = getToken() ?: return@setOnClickListener
            if (selectedThemeId == null) {
                Toast.makeText(this, "Hãy chọn một chủ đề trước!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.getRandomExam(
                token          = token,
                themeId        = selectedThemeId!!,
                difficulty     = selectedDifficulty,
                totalQuestions = selectedCount,
                questionTypes  = defaultQuestionTypes
            )
        }
    }

    private fun setupObservers() {
        viewModel.themes.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.let { themeAdapter.updateData(it) }
                is Resource.Error   -> Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }

        viewModel.randomExam.observe(this) { resource ->
            val btnStart = findViewById<Button>(R.id.btn_start)
            when (resource) {
                is Resource.Loading -> {
                    btnStart.isEnabled = false
                    btnStart.text = "Đang tải..."
                }
                is Resource.Success -> {
                    btnStart.isEnabled = true
                    btnStart.text = "Bắt đầu"
                    if (!isNavigating) {
                        isNavigating = true
                        resource.data?.let { examData ->
                            val intent = Intent(this, QuizActivity::class.java).apply {
                                putExtra("EXAM_DATA", Gson().toJson(examData))
                            }
                            startActivity(intent)
                        }
                    }
                }
                is Resource.Error -> {
                    btnStart.isEnabled = true
                    btnStart.text = "Bắt đầu"
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateCountDisplay(tvValue: TextView, btnMinus: TextView, btnPlus: TextView) {
        tvValue.text   = "$selectedCount câu"
        btnMinus.alpha = if (selectedCount <= minCount) 0.3f else 1f
        btnPlus.alpha  = if (selectedCount >= maxCount) 0.3f else 1f
    }

    private fun updateDifficultyUI(diff: Int, selected: TextView, vararg unselected: TextView) {
        selectedDifficulty = diff
        selected.setBackgroundResource(R.drawable.bg_button_orange_grad)
        selected.setTextColor(Color.WHITE)
        unselected.forEach {
            it.setBackgroundColor(Color.TRANSPARENT)
            it.setTextColor(Color.parseColor("#6B7280"))
        }
    }
}