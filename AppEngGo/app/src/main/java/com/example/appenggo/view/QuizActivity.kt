package com.example.appenggo.view

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.Resource
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.Response.ExamQuestionWrapper
import com.example.appenggo.model.Response.SubmitExamResponse
import com.example.appenggo.repository.ThemeRepository
import com.example.appenggo.viewmodel.QuizViewModel
import com.example.appenggo.viewmodel.VocabularyViewModelFactory
import com.google.android.material.card.MaterialCardView

class QuizActivity : AppCompatActivity() {

    private lateinit var viewModel: QuizViewModel
    private lateinit var tvTimer: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvQuestionContent: TextView
    private lateinit var containerAnswers: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnBack: ImageView

    private var selectedLeftId: Int? = null
    private var selectedRightId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        initViews()
        setupViewModel()
        setupObservers()

        val examId = intent.getIntExtra("EXAM_ID", -1)
        val token = getToken()

        if (examId != -1 && token != null) {
            viewModel.startExam(token, examId)
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin đề thi", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPref.getString("TOKEN", null)
    }

    private fun initViews() {
        tvTimer = findViewById(R.id.tv_timer)
        tvQuestionNumber = findViewById(R.id.tv_question_number)
        tvQuestionContent = findViewById(R.id.tv_question_content)
        containerAnswers = findViewById(R.id.container_answers)
        progressBar = findViewById(R.id.quiz_progress)
        btnPrev = findViewById(R.id.btn_prev)
        btnNext = findViewById(R.id.btn_next)
        btnBack = findViewById(R.id.btn_back)

        btnBack.setOnClickListener { showExitConfirmation() }
        btnPrev.setOnClickListener { viewModel.previousQuestion() }
        btnNext.setOnClickListener {
            val exam = viewModel.examData.value?.data
            val currentIndex = viewModel.currentQuestionIndex.value ?: 0
            if (exam != null && currentIndex == exam.questions.size - 1) {
                submitExam()
            } else {
                viewModel.nextQuestion()
            }
        }
    }

    private fun setupViewModel() {
        val repository = ThemeRepository(RetrofitClient.api)
        val factory = VocabularyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[QuizViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.examData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    resource.data?.let { exam ->
                        findViewById<TextView>(R.id.tv_quiz_title).text = exam.title
                        progressBar.max = exam.questions.size
                        updateQuestion(viewModel.currentQuestionIndex.value ?: 0)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.currentQuestionIndex.observe(this) { index ->
            updateQuestion(index)
        }

        viewModel.timeLeft.observe(this) { time ->
            tvTimer.text = time
            if (time == "00:00") {
                submitExam()
            }
        }

        viewModel.submitResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Toast.makeText(this, "Đang nộp bài...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    resource.data?.let { result ->
                        navigateToResult(result)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, "Nộp bài thất bại: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateQuestion(index: Int) {
        val exam = viewModel.examData.value?.data ?: return
        if (index < 0 || index >= exam.questions.size) return

        selectedLeftId = null
        selectedRightId = null

        val wrapper = exam.questions[index]
        val question = wrapper.question

        tvQuestionNumber.text = "Câu hỏi ${index + 1}/${exam.questions.size}"
        
        if (question.questionType == "FILL_BLANK") {
            var displayedContent = question.content
            question.fillBlankOptions?.sortedBy { it.position }?.forEach { opt ->
                displayedContent = displayedContent.replaceFirst("__", " (____) ")
            }
            tvQuestionContent.text = displayedContent
        } else {
            tvQuestionContent.text = question.content
        }

        progressBar.progress = index + 1
        renderAnswers(wrapper)

        btnPrev.visibility = if (index == 0) View.GONE else View.VISIBLE
        btnNext.text = if (index == exam.questions.size - 1) "Nộp bài" else "Tiếp theo"
    }

    private fun getOptionLetter(index: Int): String = ('A' + index).toString()

    private fun renderAnswers(wrapper: ExamQuestionWrapper) {
        containerAnswers.removeAllViews()
        val question = wrapper.question
        val userAnswers = viewModel.userAnswers.value ?: emptyMap()
        val density = resources.displayMetrics.density

        when (question.questionType) {
            "MULTIPLE_CHOICE" -> {
                question.multipleOptions?.forEachIndexed { index, option ->
                    val isSelected = userAnswers[question.id] == option.id
                    val card = MaterialCardView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, (14 * density).toInt()) }
                        radius = 20f * density
                        cardElevation = if (isSelected) 6f * density else 1f * density
                        strokeWidth = if (isSelected) (2.5 * density).toInt() else (1 * density).toInt()
                        strokeColor = if (isSelected) ContextCompat.getColor(this@QuizActivity, R.color.primary_blue) else Color.parseColor("#E0E0E0")
                        setCardBackgroundColor(if (isSelected) Color.parseColor("#F0F7FF") else Color.WHITE)
                        
                        val layout = LinearLayout(this@QuizActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding((20 * density).toInt(), (18 * density).toInt(), (20 * density).toInt(), (18 * density).toInt())
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        
                        val circleLabel = TextView(this@QuizActivity).apply {
                            layoutParams = LinearLayout.LayoutParams((32 * density).toInt(), (32 * density).toInt()).apply { marginEnd = (16 * density).toInt() }
                            text = getOptionLetter(index)
                            gravity = Gravity.CENTER
                            textSize = 15f
                            setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#9E9E9E"))
                            setTypeface(null, Typeface.BOLD)
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(if (isSelected) ContextCompat.getColor(this@QuizActivity, R.color.primary_blue) else Color.parseColor("#F5F5F5"))
                            }
                        }
                        
                        val text = TextView(this@QuizActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                            text = option.optionText
                            setTextColor(if (isSelected) ContextCompat.getColor(this@QuizActivity, R.color.primary_blue) else Color.parseColor("#333333"))
                            textSize = 16f
                            if (isSelected) setTypeface(null, Typeface.BOLD)
                        }
                        
                        layout.addView(circleLabel)
                        layout.addView(text)
                        if (isSelected) {
                            val check = ImageView(this@QuizActivity).apply {
                                layoutParams = LinearLayout.LayoutParams((24 * density).toInt(), (24 * density).toInt())
                                setImageResource(android.R.drawable.checkbox_on_background)
                                setColorFilter(ContextCompat.getColor(this@QuizActivity, R.color.primary_blue))
                            }
                            layout.addView(check)
                        }
                        addView(layout)
                        setOnClickListener {
                            viewModel.saveAnswer(question.id, option.id)
                            renderAnswers(wrapper)
                        }
                    }
                    containerAnswers.addView(card)
                }
            }
            "FILL_BLANK" -> {
                question.fillBlankOptions?.sortedBy { it.position }?.forEach { option ->
                    val container = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, (16 * density).toInt()) }
                    }
                    val label = TextView(this).apply {
                        text = "Vị trí (${option.position})"
                        textSize = 13f
                        setTextColor(Color.parseColor("#757575"))
                        setTypeface(null, Typeface.BOLD)
                        setPadding((4 * density).toInt(), 0, 0, (8 * density).toInt())
                    }
                    val inputCard = MaterialCardView(this).apply {
                        radius = 16f * density
                        setCardBackgroundColor(Color.WHITE)
                        cardElevation = 2f * density
                        val editText = EditText(this@QuizActivity).apply {
                            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            setPadding((16 * density).toInt(), (14 * density).toInt(), (16 * density).toInt(), (14 * density).toInt())
                            hint = option.placeholder ?: "Nhập đáp án..."
                            background = null
                            textSize = 16f
                            setTextColor(Color.BLACK)
                            filters = arrayOf(InputFilter.LengthFilter(option.maxLength))
                            
                            val savedData = userAnswers[question.id] as? Map<Int, String>
                            setText(savedData?.get(option.blankId) ?: "")
                            
                            addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                override fun afterTextChanged(s: Editable?) {
                                    val currentMap = (viewModel.userAnswers.value?.get(question.id) as? Map<Int, String>)?.toMutableMap() ?: mutableMapOf()
                                    currentMap[option.blankId] = s.toString()
                                    viewModel.saveAnswer(question.id, currentMap)
                                }
                            })
                        }
                        addView(editText)
                    }
                    container.addView(label)
                    container.addView(inputCard)
                    containerAnswers.addView(container)
                }
            }
            "MATCHING" -> {
                val pairs = (userAnswers[question.id] as? Map<Int, Int>) ?: emptyMap()
                
                if (pairs.isNotEmpty()) {
                    val headerMatched = TextView(this).apply {
                        text = "CÁC CẶP ĐÃ KẾT NỐI (BẤM ĐỂ HỦY)"
                        textSize = 10f
                        letterSpacing = 0.1f
                        setTextColor(Color.parseColor("#4CAF50"))
                        setTypeface(null, Typeface.BOLD)
                        setPadding((4 * density).toInt(), 0, 0, (12 * density).toInt())
                    }
                    containerAnswers.addView(headerMatched)

                    pairs.forEach { (leftId, rightId) ->
                        val leftText = question.leftOptions?.find { it.id == leftId }?.optionText ?: ""
                        val rightText = question.rightOptions?.find { it.id == rightId }?.optionText ?: ""
                        
                        val matchedCard = MaterialCardView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, (10 * density).toInt()) }
                            radius = 16f * density
                            setCardBackgroundColor(Color.parseColor("#F1F8E9"))
                            strokeColor = Color.parseColor("#C5E1A5")
                            strokeWidth = (1.5 * density).toInt()
                            cardElevation = 2f
                            
                            val row = LinearLayout(this@QuizActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding((16 * density).toInt(), (14 * density).toInt(), (16 * density).toInt(), (14 * density).toInt())
                                gravity = Gravity.CENTER_VERTICAL
                                weightSum = 2.4f
                            }
                            
                            val tvL = TextView(this@QuizActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                                text = leftText
                                gravity = Gravity.CENTER
                                setTextColor(Color.parseColor("#2E7D32"))
                                setTypeface(null, Typeface.BOLD)
                                textSize = 15f
                            }
                            
                            val ivLink = ImageView(this@QuizActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(0, (20 * density).toInt(), 0.4f)
                                setImageResource(android.R.drawable.ic_menu_share)
                                rotation = 90f
                                setColorFilter(Color.parseColor("#689F38"))
                            }

                            val tvR = TextView(this@QuizActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                                text = rightText
                                gravity = Gravity.CENTER
                                setTextColor(Color.parseColor("#2E7D32"))
                                setTypeface(null, Typeface.BOLD)
                                textSize = 15f
                            }
                            
                            row.addView(tvL)
                            row.addView(ivLink)
                            row.addView(tvR)
                            addView(row)

                            setOnClickListener {
                                val newPairs = pairs.toMutableMap().apply { remove(leftId) }
                                viewModel.saveAnswer(question.id, newPairs)
                                renderAnswers(wrapper)
                            }
                        }
                        containerAnswers.addView(matchedCard)
                    }
                    containerAnswers.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, (16 * density).toInt()) })
                }

                val columns = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    weightSum = 2f
                }
                val leftPane = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = (8 * density).toInt() }
                }
                val rightPane = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = (8 * density).toInt() }
                }

                question.leftOptions?.filter { it.id !in pairs.keys }?.forEach { item ->
                    val isSelected = selectedLeftId == item.id
                    val card = createMatchingCard(item.optionText, isSelected, density)
                    card.setOnClickListener {
                        selectedLeftId = if (selectedLeftId == item.id) null else item.id
                        checkMatching(question.id, wrapper)
                    }
                    leftPane.addView(card)
                }

                question.rightOptions?.filter { it.id !in pairs.values }?.forEach { item ->
                    val isSelected = selectedRightId == item.id
                    val card = createMatchingCard(item.optionText, isSelected, density)
                    card.setOnClickListener {
                        selectedRightId = if (selectedRightId == item.id) null else item.id
                        checkMatching(question.id, wrapper)
                    }
                    rightPane.addView(card)
                }

                columns.addView(leftPane)
                columns.addView(rightPane)
                containerAnswers.addView(columns)
            }
        }
    }

    private fun createMatchingCard(txt: String, isSelected: Boolean, density: Float): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, (12 * density).toInt()) }
            radius = 16f * density
            strokeWidth = if (isSelected) (3 * density).toInt() else (1 * density).toInt()
            strokeColor = if (isSelected) ContextCompat.getColor(this@QuizActivity, R.color.primary_blue) else Color.parseColor("#E0E0E0")
            setCardBackgroundColor(if (isSelected) Color.parseColor("#F0F7FF") else Color.WHITE)
            cardElevation = if (isSelected) 4f * density else 1f * density
            
            val tv = TextView(this@QuizActivity).apply {
                text = txt
                setPadding((16 * density).toInt(), (18 * density).toInt(), (16 * density).toInt(), (18 * density).toInt())
                gravity = Gravity.CENTER
                setTextColor(if (isSelected) ContextCompat.getColor(this@QuizActivity, R.color.primary_blue) else Color.parseColor("#333333"))
                textSize = 15f
                if (isSelected) setTypeface(null, Typeface.BOLD)
            }
            addView(tv)
        }
    }

    private fun checkMatching(questionId: Int, wrapper: ExamQuestionWrapper) {
        val l = selectedLeftId
        val r = selectedRightId
        if (l != null && r != null) {
            val pairs = (viewModel.userAnswers.value?.get(questionId) as? Map<Int, Int>)?.toMutableMap() ?: mutableMapOf()
            pairs[l] = r
            viewModel.saveAnswer(questionId, pairs)
            selectedLeftId = null
            selectedRightId = null
        }
        renderAnswers(wrapper)
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Thoát bài thi?")
            .setMessage("Tiến độ làm bài của bạn sẽ không được lưu lại.")
            .setPositiveButton("Thoát") { _, _ -> finish() }
            .setNegativeButton("Ở lại", null)
            .show()
    }

    private fun navigateToResult(result: SubmitExamResponse) {
        val intent = Intent(this, QuizResultActivity::class.java).apply {
            putExtra("CORRECT_COUNT", result.correctAnswersCount)
            putExtra("TOTAL_QUESTIONS", result.totalQuestions)
            putExtra("SCORE", result.totalScore)
            // Giả sử startedAt và completedAt có thể tính ra thời gian, hoặc dùng timeTakenSeconds nếu backend trả về
            putExtra("TIME_TAKEN", "05:20") 
        }
        startActivity(intent)
        finish()
    }

    private fun submitExam() {
        val token = getToken()
        if (token != null) {
            viewModel.submitExam(token)
        } else {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show()
        }
    }
}
