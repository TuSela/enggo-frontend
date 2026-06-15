package com.example.appenggo.view

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import com.example.appenggo.model.ExamQuestionWrapper
import com.example.appenggo.model.StartExamResponse
import com.example.appenggo.model.SubmitExamResponse
import com.example.appenggo.repository.ThemeRepository
import com.example.appenggo.viewmodel.QuizViewModel
import com.example.appenggo.viewmodel.VocabularyViewModelFactory
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson

class QuizActivity : AppCompatActivity() {

    private lateinit var viewModel: QuizViewModel
    private lateinit var tvTimer: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvQuestionContent: TextView
    private lateinit var tvQuestionLabel: TextView   // nhãn loại câu hỏi
    private lateinit var containerAnswers: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnBack: ImageView

    // Màu chủ đạo (cam như trong ảnh)
    private val colorOrange     get() = Color.parseColor("#FF8C00")
    private val colorOrangeLight get() = Color.parseColor("#FFF3E0")
    private val colorOrangeStroke get() = Color.parseColor("#FF8C00")
    private val colorGray       get() = Color.parseColor("#F5F5F5")
    private val colorGrayStroke get() = Color.parseColor("#E0E0E0")
    private val colorTextDark   get() = Color.parseColor("#1A1A1A")
    private val colorTextGray   get() = Color.parseColor("#9E9E9E")

    private var selectedLeftId: Int? = null
    private var selectedRightId: Int? = null

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        initViews()
        setupViewModel()
        setupObservers()
        loadExam()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun getToken(): String? =
        getSharedPreferences("app_prefs", MODE_PRIVATE).getString("TOKEN", null)

    private fun dp(value: Float) = (value * resources.displayMetrics.density).toInt()

    // ── Load ──────────────────────────────────────────────────────────────────

    private fun loadExam() {
        val examJson = intent.getStringExtra("EXAM_DATA")
        if (examJson != null) {
            try {
                val examData = Gson().fromJson(examJson, StartExamResponse::class.java)
                viewModel.loadExamData(examData)
            } catch (e: Exception) {
                Toast.makeText(this, "Lỗi đọc dữ liệu đề thi", Toast.LENGTH_SHORT).show()
                finish()
            }
            return
        }
        val examId = intent.getIntExtra("EXAM_ID", -1)
        val token  = getToken()
        if (examId != -1 && token != null) {
            viewModel.startExam(token, examId)
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin đề thi", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    private fun initViews() {
        tvTimer           = findViewById(R.id.tv_timer)
        tvQuestionNumber  = findViewById(R.id.tv_question_number)
        tvQuestionContent = findViewById(R.id.tv_question_content)
        tvQuestionLabel   = findViewById(R.id.tv_question_label)
        containerAnswers  = findViewById(R.id.container_answers)
        progressBar       = findViewById(R.id.quiz_progress)
        btnPrev           = findViewById(R.id.btn_prev)
        btnNext           = findViewById(R.id.btn_next)
        btnBack           = findViewById(R.id.btn_back)

        btnBack.setOnClickListener { showExitConfirmation() }
        btnPrev.setOnClickListener { viewModel.previousQuestion() }
        btnNext.setOnClickListener {
            val exam         = viewModel.examData.value?.data
            val currentIndex = viewModel.currentQuestionIndex.value ?: 0
            if (exam != null && currentIndex == exam.questions.size - 1) submitExam()
            else viewModel.nextQuestion()
        }
    }

    private fun setupViewModel() {
        val repository = ThemeRepository(RetrofitClient.api)
        val factory    = VocabularyViewModelFactory(repository)
        viewModel      = ViewModelProvider(this, factory)[QuizViewModel::class.java]
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun setupObservers() {
        viewModel.examData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> resource.data?.let { exam ->
                    findViewById<TextView>(R.id.tv_quiz_title).text = exam.title
                    progressBar.max = exam.questions.size
                    updateQuestion(viewModel.currentQuestionIndex.value ?: 0)
                }
                is Resource.Error -> Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.currentQuestionIndex.observe(this) { index -> updateQuestion(index) }

        viewModel.timeLeft.observe(this) { time ->
            tvTimer.text = time
            // Đổi màu timer khi còn ít thời gian
            if (time != null) {
                val parts = time.split(":")
                val totalSecs = (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 +
                        (parts.getOrNull(1)?.toIntOrNull() ?: 0)
                tvTimer.setTextColor(if (totalSecs <= 60) Color.parseColor("#F44336") else Color.WHITE)
            }
            if (time == "00:00") submitExam()
        }

        viewModel.submitResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> Toast.makeText(this, "Đang nộp bài...", Toast.LENGTH_SHORT).show()
                is Resource.Success -> resource.data?.let { navigateToResult(it) }
                is Resource.Error   -> Toast.makeText(this, "Nộp bài thất bại: ${resource.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ── Update question ───────────────────────────────────────────────────────

    private fun updateQuestion(index: Int) {
        val exam = viewModel.examData.value?.data ?: return
        if (index < 0 || index >= exam.questions.size) return

        selectedLeftId  = null
        selectedRightId = null

        val wrapper  = exam.questions[index]
        val question = wrapper.question

        tvQuestionNumber.text = "${index + 1} / ${exam.questions.size}"

        // Nhãn loại câu hỏi
        tvQuestionLabel.text = when (question.questionType) {
            "MULTIPLE_CHOICE" -> "CHỌN ĐÁP ÁN ĐÚNG ĐỂ HOÀN THÀNH CÂU:"
            "FILL_BLANK"      -> "Điền vào chỗ trống"
            "MATCHING"        -> "Ghép các cặp từ đồng nghĩa"
            else              -> ""
        }

        // Nội dung câu hỏi – FILL_BLANK hiển thị dấu ___
        if (question.questionType == "FILL_BLANK") {
            var displayed = question.content
            question.fillBlankOptions?.sortedBy { it.position }?.forEach { _ ->
                displayed = displayed.replaceFirst("__", "______")
            }
            tvQuestionContent.text = displayed
        } else {
            tvQuestionContent.text = question.content
        }

        progressBar.progress = index + 1
        renderAnswers(wrapper)

        btnPrev.visibility = if (index == 0) View.GONE else View.VISIBLE
        btnNext.text       = if (index == exam.questions.size - 1) "NỘP BÀI" else "TIẾP THEO"
    }

    // ── Render answers ────────────────────────────────────────────────────────

    private fun renderAnswers(wrapper: ExamQuestionWrapper) {
        containerAnswers.removeAllViews()
        containerAnswers.weightSum = 0f  // reset trước mỗi lần render
        val question    = wrapper.question
        val userAnswers = viewModel.userAnswers.value ?: emptyMap()

        when (question.questionType) {
            "MULTIPLE_CHOICE" -> renderMultipleChoice(question, userAnswers, wrapper)
            "FILL_BLANK"      -> renderFillBlank(question, userAnswers)
            "MATCHING"        -> renderMatching(question, userAnswers, wrapper)
        }
    }

    // ── Multiple Choice ───────────────────────────────────────────────────────

    private fun renderMultipleChoice(
        question: com.example.appenggo.model.QuestionDetail,
        userAnswers: Map<Int, Any>,
        wrapper: ExamQuestionWrapper
    ) {
        val selectedId = userAnswers[question.id] as? Int

        question.multipleOptions?.forEach { option ->
            val isSelected = selectedId == option.id

            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, dp(12f)) }
                radius      = dp(14f).toFloat()
                strokeWidth = dp(if (isSelected) 2f else 1f)
                strokeColor = if (isSelected) colorOrangeStroke else colorGrayStroke
                setCardBackgroundColor(if (isSelected) colorOrangeLight else Color.WHITE)
                cardElevation = 0f

                val row = LinearLayout(this@QuizActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(dp(20f), dp(22f), dp(20f), dp(22f))
                    gravity = Gravity.CENTER_VERTICAL
                    minimumHeight = dp(80f)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                val tvText = TextView(this@QuizActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    text = option.optionText
                    textSize = 16f
                    setTextColor(if (isSelected) colorOrange else colorTextDark)
                    if (isSelected) setTypeface(null, Typeface.BOLD)
                }

                // Checkmark badge khi đã chọn
                if (isSelected) {
                    val badge = TextView(this@QuizActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dp(26f), dp(26f)).apply {
                            marginStart = dp(12f)
                        }
                        text = "✓"
                        textSize = 14f
                        gravity = Gravity.CENTER
                        setTextColor(Color.WHITE)
                        setTypeface(null, Typeface.BOLD)
                        background = createCircleBg(colorOrange)
                    }
                    row.addView(tvText)
                    row.addView(badge)
                } else {
                    row.addView(tvText)
                }

                addView(row)
                setOnClickListener {
                    viewModel.saveAnswer(question.id, option.id)
                    renderAnswers(wrapper)
                }
            }
            containerAnswers.addView(card)
        }
    }

    // ── Fill Blank ────────────────────────────────────────────────────────────

    private fun renderFillBlank(
        question: com.example.appenggo.model.QuestionDetail,
        userAnswers: Map<Int, Any>
    ) {
        val savedMap = (userAnswers[question.id] as? Map<Int, String>) ?: emptyMap()
        val options  = question.fillBlankOptions?.sortedBy { it.position } ?: return

        // Một card duy nhất chứa tất cả các ô trống
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            radius        = dp(14f).toFloat()
            strokeWidth   = dp(1f)
            strokeColor   = colorGrayStroke
            setCardBackgroundColor(Color.WHITE)
            cardElevation = 0f
        }

        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20f), dp(20f), dp(20f), dp(24f))
        }

        options.forEach { opt ->
            val tvLabel = TextView(this).apply {
                text = "Ô trống ${opt.position}"
                textSize = 12f
                setTextColor(colorTextGray)
                setTypeface(null, Typeface.BOLD)
                letterSpacing = 0.05f
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = if (opt.position > 1) dp(20f) else 0 }
            }

            val editText = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(6f) }
                hint        = opt.placeholder ?: "Nhập đáp án..."
                filters     = arrayOf(InputFilter.LengthFilter(opt.maxLength))
                setText(savedMap[opt.blankId] ?: "")
                textSize    = 16f
                maxLines    = 1
                isSingleLine = true
                inputType   = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                imeOptions  = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                setTextColor(colorTextDark)
                setHintTextColor(Color.parseColor("#BDBDBD"))
                background  = createUnderlineBg(colorOrange)
                setPadding(dp(2f), dp(8f), dp(2f), dp(10f))

                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        val current = (viewModel.userAnswers.value?.get(question.id)
                                as? Map<Int, String>)?.toMutableMap() ?: mutableMapOf()
                        current[opt.blankId] = s.toString()
                        viewModel.saveAnswer(question.id, current)
                    }
                    override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                    override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                })
            }

            inner.addView(tvLabel)
            inner.addView(editText)
        }

        card.addView(inner)
        containerAnswers.addView(card)
    }

    // ── Matching ──────────────────────────────────────────────────────────────

    private fun renderMatching(
        question: com.example.appenggo.model.QuestionDetail,
        userAnswers: Map<Int, Any>,
        wrapper: ExamQuestionWrapper
    ) {
        val pairs = (userAnswers[question.id] as? Map<Int, Int>) ?: emptyMap()
        val itemHeight = dp(110f)

        // Các cặp đã ghép – hiển thị dạng card ngang với đường cam nối
        pairs.forEach { (leftId, rightId) ->
            val leftText  = question.leftOptions?.find  { it.id == leftId  }?.optionText ?: ""
            val rightText = question.rightOptions?.find { it.id == rightId }?.optionText ?: ""

            val matchedCard = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    itemHeight
                ).apply { setMargins(0, 0, 0, dp(12f)) }
                radius = dp(14f).toFloat()
                strokeWidth = dp(2f)
                strokeColor = colorOrangeStroke
                setCardBackgroundColor(Color.WHITE)
                cardElevation = 0f

                val row = LinearLayout(this@QuizActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(dp(16f), 0, dp(16f), 0)
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    weightSum = 2.2f
                }

                val tvL = TextView(this@QuizActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    text = leftText
                    gravity = Gravity.CENTER
                    textSize = 15f
                    setTextColor(colorTextDark)
                    setTypeface(null, Typeface.BOLD)
                }

                // Đường cam nối giữa
                val connector = View(this@QuizActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, dp(2.5f), 0.2f).apply {
                        marginStart = dp(10f)
                        marginEnd   = dp(10f)
                    }
                    setBackgroundColor(colorOrange)
                }

                val tvR = TextView(this@QuizActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    text = rightText
                    gravity = Gravity.CENTER
                    textSize = 15f
                    setTextColor(colorTextDark)
                    setTypeface(null, Typeface.BOLD)
                }

                row.addView(tvL)
                row.addView(connector)
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

        // Spacer
        if (pairs.isNotEmpty()) {
            containerAnswers.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(4f))
            })
        }

        // Hai cột trái / phải cho các item chưa ghép
        val columns = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum   = 2f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val leftPane = LinearLayout(this).apply {
            orientation  = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginEnd = dp(6f) }
        }
        val rightPane = LinearLayout(this).apply {
            orientation  = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginStart = dp(6f) }
        }

        question.leftOptions?.filter { it.id !in pairs.keys }?.forEach { item ->
            val card = createMatchingItemCard(item.optionText, selectedLeftId == item.id, itemHeight)
            card.setOnClickListener {
                selectedLeftId = if (selectedLeftId == item.id) null else item.id
                checkMatching(question.id, wrapper)
            }
            leftPane.addView(card)
        }

        question.rightOptions?.filter { it.id !in pairs.values }?.forEach { item ->
            val card = createMatchingItemCard(item.optionText, selectedRightId == item.id, itemHeight)
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

    // Card cho từng item matching chưa ghép
    private fun createMatchingItemCard(label: String, isSelected: Boolean, height: Int): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            ).apply { setMargins(0, 0, 0, dp(12f)) }
            radius      = dp(14f).toFloat()
            strokeWidth = dp(if (isSelected) 2.5f else 1f)
            strokeColor = if (isSelected) colorOrangeStroke else colorGrayStroke
            setCardBackgroundColor(if (isSelected) colorOrangeLight else Color.WHITE)
            cardElevation = 0f

            addView(TextView(this@QuizActivity).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                text = label
                setPadding(dp(12f), dp(8f), dp(12f), dp(8f))
                gravity   = Gravity.CENTER
                textSize  = 14f
                setTextColor(if (isSelected) colorOrange else colorTextDark)
                if (isSelected) setTypeface(null, Typeface.BOLD)
            })
        }
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────

    /** Tạo background hình tròn cho badge check */
    private fun createCircleBg(color: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(color)
        }
    }

    /** Tạo background underline cho EditText (không có viền bao quanh) */
    private fun createUnderlineBg(color: Int): android.graphics.drawable.LayerDrawable {
        val underline = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(color)
        }
        return android.graphics.drawable.LayerDrawable(arrayOf(underline)).apply {
            setLayerInset(0, 0, dp(36f), 0, 0)
        }
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    private fun checkMatching(questionId: Int, wrapper: ExamQuestionWrapper) {
        val l = selectedLeftId
        val r = selectedRightId
        if (l != null && r != null) {
            val pairs = (viewModel.userAnswers.value?.get(questionId) as? Map<Int, Int>)
                ?.toMutableMap() ?: mutableMapOf()
            pairs[l] = r
            viewModel.saveAnswer(questionId, pairs)
            selectedLeftId  = null
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
        val timeTaken: String = if (result.timeTakenSeconds != null) {
            String.format("%02d:%02d", result.timeTakenSeconds / 60, result.timeTakenSeconds % 60)
        } else {
            try {
                val fmt   = java.time.LocalDateTime.parse(result.completedAt.substringBefore("."))
                val start = java.time.LocalDateTime.parse(result.startedAt.substringBefore("."))
                val secs  = java.time.Duration.between(start, fmt).seconds
                String.format("%02d:%02d", secs / 60, secs % 60)
            } catch (e: Exception) {
                "--:--"
            }
        }

        val intent = Intent(this, QuizResultActivity::class.java).apply {
            putExtra("CORRECT_COUNT",   result.correctAnswersCount)
            putExtra("TOTAL_QUESTIONS", result.totalQuestions)
            putExtra("SCORE",           result.totalScore)
            putExtra("TIME_TAKEN",      timeTaken)
            putExtra("EXP_GAINED",      result.expGained)
        }
        startActivity(intent)
        finish()
    }

    private fun submitExam() {
        val token = getToken()
        if (token != null) viewModel.submitExam(token)
        else Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show()
    }
}
