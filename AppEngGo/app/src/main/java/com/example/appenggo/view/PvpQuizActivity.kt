package com.example.appenggo.view

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.Resource
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.*
import com.example.appenggo.repository.ThemeRepository
import com.example.appenggo.viewmodel.QuizViewModel
import com.example.appenggo.viewmodel.VocabularyViewModelFactory
import com.example.appenggo.websocket.WebSocketManager
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson

class PvpQuizActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXAM_DATA      = "EXAM_DATA"
        const val EXTRA_MATCH_ID       = "PVP_MATCH_ID"
        const val EXTRA_IS_PLAYER1     = "IS_PLAYER1"
        const val EXTRA_OPPONENT_NAME  = "OPPONENT_NAME"
    }

    private lateinit var viewModel: QuizViewModel

    // Header
    private lateinit var tvTimer: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var progressBar: ProgressBar

    // PVP score bar
    private lateinit var tvMyScore: TextView
    private lateinit var tvOpponentScore: TextView
    private lateinit var tvMyName: TextView
    private lateinit var tvOpponentName: TextView

    // Body
    private lateinit var tvQuestionLabel: TextView
    private lateinit var tvQuestionContent: TextView
    private lateinit var containerAnswers: LinearLayout

    // State
    private var pvpMatchId  = -1
    private var isPlayer1   = false
    private var myScore     = 0
    private var opponentScore = 0
    private var myUserId    = -1
    private var isAnswered  = false   // Đã trả lời câu hiện tại chưa

    private val gson    = Gson()
    private val handler = Handler(Looper.getMainLooper())

    // Màu
    private val colorOrange      get() = Color.parseColor("#FF8C00")
    private val colorOrangeLight get() = Color.parseColor("#FFF3E0")
    private val colorGreenLight  get() = Color.parseColor("#E8F5E9")
    private val colorRedLight    get() = Color.parseColor("#FFEBEE")
    private val colorGrayStroke  get() = Color.parseColor("#E0E0E0")
    private val colorOrangeStroke get() = Color.parseColor("#FF8C00")
    private val colorTextDark    get() = Color.parseColor("#1A1A1A")
    private val colorTextGray    get() = Color.parseColor("#9E9E9E")

    private fun dp(v: Float) = (v * resources.displayMetrics.density).toInt()

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pvp_quiz)

        pvpMatchId = intent.getIntExtra(EXTRA_MATCH_ID, -1)
        isPlayer1  = intent.getBooleanExtra(EXTRA_IS_PLAYER1, false)
        val opponentName = intent.getStringExtra(EXTRA_OPPONENT_NAME) ?: "Đối thủ"

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        myUserId = prefs.getInt("USER_ID", -1)
        val myUsername = prefs.getString("USERNAME", "Bạn") ?: "Bạn"

        initViews()
        setupScoreBar(myUsername, opponentName)
        setupViewModel()
        setupObservers()
        loadExam()
        setupBackPress()
        listenPvpProgress()
        listenPvpResult()
    }

    private fun initViews() {
        tvTimer          = findViewById(R.id.tv_timer)
        tvQuestionNumber = findViewById(R.id.tv_question_number)
        progressBar      = findViewById(R.id.quiz_progress)
        tvMyScore        = findViewById(R.id.tv_my_score)
        tvOpponentScore  = findViewById(R.id.tv_opponent_score)
        tvMyName         = findViewById(R.id.tv_my_name)
        tvOpponentName   = findViewById(R.id.tv_opponent_name)
        tvQuestionLabel  = findViewById(R.id.tv_question_label)
        tvQuestionContent= findViewById(R.id.tv_question_content)
        containerAnswers = findViewById(R.id.container_answers)

        // Không có nút back trong PVP (để tránh thoát giữa chừng)
        // Nếu muốn có thể thêm dialog cảnh báo
    }

    private fun setupScoreBar(myUsername: String, opponentName: String) {
        tvMyName.text       = myUsername
        tvOpponentName.text = opponentName
        tvMyScore.text      = "0"
        tvOpponentScore.text= "0"
    }

    private fun setupViewModel() {
        val repository = ThemeRepository(RetrofitClient.api)
        val factory    = VocabularyViewModelFactory(repository)
        viewModel      = ViewModelProvider(this, factory)[QuizViewModel::class.java]
    }

    private fun loadExam() {
        val examJson = intent.getStringExtra(EXTRA_EXAM_DATA) ?: run {
            Toast.makeText(this, "Không có dữ liệu đề thi", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        try {
            val exam = gson.fromJson(examJson, StartExamResponse::class.java)
            viewModel.loadExamData(exam)
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi đọc đề thi: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // ── Observers ─────────────────────────────────────────────────────────────
    private fun setupObservers() {
        viewModel.examData.observe(this) { resource ->
            if (resource is Resource.Success) {
                resource.data?.let { exam ->
                    progressBar.max = exam.questions.size
                    renderQuestion(viewModel.currentQuestionIndex.value ?: 0)
                }
            }
        }

        viewModel.currentQuestionIndex.observe(this) { index ->
            renderQuestion(index)
        }

        viewModel.timeLeft.observe(this) { time ->
            tvTimer.text = time
            if (time != null) {
                val parts = time.split(":")
                val secs  = (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 +
                        (parts.getOrNull(1)?.toIntOrNull() ?: 0)
                tvTimer.setTextColor(if (secs <= 60) Color.parseColor("#F44336") else Color.WHITE)
            }
            if (time == "00:00") submitPvp()
        }
    }

    // ── Render câu hỏi ────────────────────────────────────────────────────────
    private fun renderQuestion(index: Int) {
        val exam = viewModel.examData.value?.data ?: return
        if (index < 0 || index >= exam.questions.size) return

        isAnswered = false
        containerAnswers.removeAllViews()

        val wrapper  = exam.questions[index]
        val question = wrapper.question

        tvQuestionNumber.text = "${index + 1} / ${exam.questions.size}"
        progressBar.progress  = index + 1

        tvQuestionLabel.text = when (question.questionType) {
            "MULTIPLE_CHOICE" -> "CHỌN ĐÁP ÁN ĐÚNG"
            "FILL_BLANK"      -> "ĐIỀN VÀO CHỖ TRỐNG"
            "MATCHING"        -> "GHÉP CẶP TỪ ĐÚNG"
            else              -> ""
        }

        tvQuestionContent.text = question.content

        when (question.questionType) {
            "MULTIPLE_CHOICE" -> renderMultipleChoice(wrapper)
            "FILL_BLANK"      -> renderFillBlank(wrapper)
            "MATCHING"        -> renderMatching(wrapper)
        }
    }

    // ── Multiple Choice ───────────────────────────────────────────────────────
    private fun renderMultipleChoice(wrapper: ExamQuestionWrapper) {
        val question = wrapper.question
        question.multipleOptions?.forEach { option ->
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, dp(12f)) }
                radius        = dp(14f).toFloat()
                strokeWidth   = dp(1f)
                strokeColor   = colorGrayStroke
                setCardBackgroundColor(Color.WHITE)
                cardElevation = 0f

                addView(TextView(this@PvpQuizActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    text = option.optionText
                    textSize = 16f
                    setTextColor(colorTextDark)
                    setPadding(dp(20f), dp(22f), dp(20f), dp(22f))
                    gravity = Gravity.CENTER_VERTICAL
                    minimumHeight = dp(70f)
                })

                setOnClickListener {
                    if (isAnswered) return@setOnClickListener
                    isAnswered = true
                    viewModel.saveAnswer(question.id, option.id)

                    // Highlight đáp án đã chọn
                    strokeWidth = dp(2f)
                    strokeColor = colorOrangeStroke
                    setCardBackgroundColor(colorOrangeLight)

                    // Gửi progress lên server
                    sendProgressAndAdvance(wrapper, selectedOptionId = option.id)
                }
            }
            containerAnswers.addView(card)
        }
    }

    // ── Fill Blank ─────────────────────────────────────────────────────────────
    private fun renderFillBlank(wrapper: ExamQuestionWrapper) {
        val question = wrapper.question
        val options  = question.fillBlankOptions?.sortedBy { it.position } ?: return

        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val editTexts = mutableMapOf<Int, EditText>()

        options.forEach { opt ->
            val tvLabel = TextView(this).apply {
                text = "Ô trống ${opt.position}"
                textSize = 12f
                setTextColor(colorTextGray)
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = if (opt.position > 1) dp(16f) else 0 }
            }

            val editText = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(6f) }
                hint         = opt.placeholder ?: "Nhập đáp án..."
                filters      = arrayOf(android.text.InputFilter.LengthFilter(opt.maxLength))
                textSize     = 16f
                maxLines     = 1
                isSingleLine = true
                inputType    = android.text.InputType.TYPE_CLASS_TEXT
                setTextColor(colorTextDark)
                setHintTextColor(Color.parseColor("#BDBDBD"))
            }
            editTexts[opt.blankId] = editText

            inner.addView(tvLabel)
            inner.addView(editText)
        }

        // Nút xác nhận (thay cho auto-advance vì fill blank cần nhập)
        val btnConfirm = com.google.android.material.button.MaterialButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52f)
            ).apply { topMargin = dp(20f) }
            text = "XÁC NHẬN"
            textSize = 14f
            setBackgroundColor(colorOrange)
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setOnClickListener {
                if (isAnswered) return@setOnClickListener
                isAnswered = true

                val fillMap = mutableMapOf<Int, String>()
                editTexts.forEach { (blankId, et) -> fillMap[blankId] = et.text.toString() }
                viewModel.saveAnswer(question.id, fillMap)

                val fillBlanks = fillMap.map { (blankId, input) ->
                    val pos = question.fillBlankOptions?.find { it.blankId == blankId }?.position ?: 0
                    FillBlankAnswer(blankId, pos, input)
                }
                sendProgressAndAdvance(wrapper, fillBlanks = fillBlanks)
            }
        }

        inner.addView(btnConfirm)
        containerAnswers.addView(inner)
    }

    // ── Matching ───────────────────────────────────────────────────────────────
    private var selectedLeftId: Int? = null
    private var selectedRightId: Int? = null
    private val matchedPairs = mutableMapOf<Int, Int>()

    private fun renderMatching(wrapper: ExamQuestionWrapper) {
        val question   = wrapper.question
        val itemHeight = dp(80f)

        // Reset trạng thái
        selectedLeftId  = null
        selectedRightId = null
        matchedPairs.clear()

        rebuildMatchingUI(wrapper, itemHeight)
    }

    private fun rebuildMatchingUI(wrapper: ExamQuestionWrapper, itemHeight: Int) {
        containerAnswers.removeAllViews()
        val question = wrapper.question

        // Các cặp đã ghép
        matchedPairs.forEach { (leftId, rightId) ->
            val lt = question.leftOptions?.find  { it.id == leftId  }?.optionText ?: ""
            val rt = question.rightOptions?.find { it.id == rightId }?.optionText ?: ""

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(52f)
                ).apply { setMargins(0, 0, 0, dp(8f)) }
                setBackgroundColor(colorGreenLight)
                setPadding(dp(12f), 0, dp(12f), 0)
                gravity = Gravity.CENTER_VERTICAL
                weightSum = 2.2f
            }
            row.addView(TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                text = lt; gravity = Gravity.CENTER; textSize = 14f
                setTextColor(Color.parseColor("#2E7D32")); setTypeface(null, Typeface.BOLD)
            })
            row.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(2f), 0.2f).apply {
                    marginStart = dp(8f); marginEnd = dp(8f)
                }
                setBackgroundColor(Color.parseColor("#4CAF50"))
            })
            row.addView(TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                text = rt; gravity = Gravity.CENTER; textSize = 14f
                setTextColor(Color.parseColor("#2E7D32")); setTypeface(null, Typeface.BOLD)
            })
            containerAnswers.addView(row)
        }

        // Còn lại chưa ghép
        val remainLeft  = question.leftOptions?.filter  { it.id !in matchedPairs.keys }   ?: emptyList()
        val remainRight = question.rightOptions?.filter { it.id !in matchedPairs.values } ?: emptyList()

        if (remainLeft.isEmpty() && remainRight.isEmpty()) {
            // Tất cả đã ghép → gửi và chuyển câu
            if (!isAnswered) {
                isAnswered = true
                viewModel.saveAnswer(question.id, matchedPairs.toMap())
                val matchings = matchedPairs.map { (l, r) -> MatchingAnswer(l, r) }
                sendProgressAndAdvance(wrapper, matchings = matchings)
            }
            return
        }

        val columns = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum   = 2f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
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

        remainLeft.forEach { item ->
            val isSelected = selectedLeftId == item.id
            val card = createMatchCard(item.optionText, isSelected, itemHeight)
            card.setOnClickListener {
                selectedLeftId = if (selectedLeftId == item.id) null else item.id
                tryPairMatching(wrapper)
            }
            leftPane.addView(card)
        }

        remainRight.forEach { item ->
            val isSelected = selectedRightId == item.id
            val card = createMatchCard(item.optionText, isSelected, itemHeight)
            card.setOnClickListener {
                selectedRightId = if (selectedRightId == item.id) null else item.id
                tryPairMatching(wrapper)
            }
            rightPane.addView(card)
        }

        columns.addView(leftPane); columns.addView(rightPane)
        containerAnswers.addView(columns)
    }

    private fun tryPairMatching(wrapper: ExamQuestionWrapper) {
        val l = selectedLeftId
        val r = selectedRightId
        if (l != null && r != null) {
            matchedPairs[l] = r
            selectedLeftId  = null
            selectedRightId = null
        }
        rebuildMatchingUI(wrapper, dp(80f))
    }

    private fun createMatchCard(label: String, isSelected: Boolean, height: Int): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height
            ).apply { setMargins(0, 0, 0, dp(10f)) }
            radius        = dp(12f).toFloat()
            strokeWidth   = dp(if (isSelected) 2f else 1f)
            strokeColor   = if (isSelected) colorOrangeStroke else colorGrayStroke
            setCardBackgroundColor(if (isSelected) colorOrangeLight else Color.WHITE)
            cardElevation = 0f

            addView(TextView(this@PvpQuizActivity).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                text = label
                setPadding(dp(10f), dp(8f), dp(10f), dp(8f))
                gravity  = Gravity.CENTER
                textSize = 14f
                setTextColor(if (isSelected) colorOrange else colorTextDark)
                if (isSelected) setTypeface(null, Typeface.BOLD)
            })
        }
    }

    // ── Gửi progress + tự động sang câu tiếp ─────────────────────────────────
    private fun sendProgressAndAdvance(
        wrapper: ExamQuestionWrapper,
        selectedOptionId: Int? = null,
        fillBlanks: List<FillBlankAnswer>? = null,
        matchings: List<MatchingAnswer>? = null
    ) {
        // Gửi progress qua WebSocket
        if (pvpMatchId != -1) {
            val fillData = fillBlanks?.map { mapOf("blankId" to it.blankId, "position" to it.position, "userInput" to it.userInput) }
            val matchData = matchings?.map { mapOf("leftId" to it.leftId, "rightId" to it.rightId) }
            WebSocketManager.sendPvpProgress(pvpMatchId, wrapper.question.id, selectedOptionId, fillData, matchData)
        }

        // Đợi 400ms cho người dùng thấy lựa chọn, rồi tự chuyển câu
        handler.postDelayed({
            val exam         = viewModel.examData.value?.data ?: return@postDelayed
            val currentIndex = viewModel.currentQuestionIndex.value ?: 0

            if (currentIndex == exam.questions.size - 1) {
                // Câu cuối → submit
                submitPvp()
            } else {
                viewModel.nextQuestion()
            }
        }, 400)
    }

    // ── Lắng nghe điểm đối thủ ────────────────────────────────────────────────
    // ── Lắng nghe điểm realtime (cả mình và đối thủ) ───────────────────────
    private fun listenPvpProgress() {
        WebSocketManager.onPvpProgressReceived = { progress ->
            runOnUiThread {
                if (progress.userId == myUserId) {
                    // Server xác nhận điểm của mình
                    myScore = progress.currentScore
                    tvMyScore.text = myScore.toString()
                    if (progress.isCorrect) {
                        tvMyScore.setTextColor(Color.parseColor("#4CAF50"))
                        handler.postDelayed({ tvMyScore.setTextColor(Color.WHITE) }, 600)
                    }
                } else {
                    // Điểm của đối thủ
                    opponentScore = progress.currentScore
                    tvOpponentScore.text = opponentScore.toString()
                    if (progress.isCorrect) {
                        tvOpponentScore.setTextColor(Color.parseColor("#F44336"))
                        handler.postDelayed({ tvOpponentScore.setTextColor(Color.parseColor("#FF6B6B")) }, 600)
                    }
                }
            }
        }
    }

    // ── Lắng nghe kết quả cuối ────────────────────────────────────────────────
    private fun listenPvpResult() {
        WebSocketManager.onPvpResultReceived = { result ->
            runOnUiThread {
                val intent = Intent(this, PvpResultActivity::class.java).apply {
                    putExtra("RESULT_JSON", Gson().toJson(result))
                    putExtra("MY_USER_ID",  myUserId)
                    putExtra("IS_PLAYER1",  isPlayer1)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    // ── Submit toàn bộ bài qua WebSocket ─────────────────────────────────────
    private fun submitPvp() {
        val exam       = viewModel.examData.value?.data ?: return
        val answersMap = viewModel.userAnswers.value ?: emptyMap()

        val answerRequests = exam.questions.map { wrapper ->
            val qId        = wrapper.question.id
            val userAnswer = answersMap[qId]

            var selectedOptionId: Int? = null
            var fillBlanks: MutableList<FillBlankAnswer>? = null
            var matchings: MutableList<MatchingAnswer>?   = null

            when (wrapper.question.questionType) {
                "MULTIPLE_CHOICE" -> selectedOptionId = userAnswer as? Int
                "FILL_BLANK" -> {
                    val map = userAnswer as? Map<Int, String>
                    if (!map.isNullOrEmpty()) {
                        fillBlanks = map.map { (blankId, input) ->
                            val pos = wrapper.question.fillBlankOptions
                                ?.find { it.blankId == blankId }?.position ?: 0
                            FillBlankAnswer(blankId, pos, input)
                        }.toMutableList()
                    }
                }
                "MATCHING" -> {
                    val map = userAnswer as? Map<Int, Int>
                    if (!map.isNullOrEmpty()) {
                        matchings = map.map { (l, r) -> MatchingAnswer(l, r) }.toMutableList()
                    }
                }
            }
            ExamAnswerRequest(qId, selectedOptionId, fillBlanks, matchings)
        }

        val submitJson = Gson().toJson(mapOf("examAnswers" to answerRequests))
        WebSocketManager.submitPvpExam(pvpMatchId, submitJson)

        // Hiện màn chờ đối thủ
        showWaitingOverlay()
    }

    private fun showWaitingOverlay() {
        val overlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity     = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#CC000000"))
            layoutParams = android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        overlay.addView(ProgressBar(this))
        overlay.addView(TextView(this).apply {
            text = "Đã nộp bài!\nĐang chờ đối thủ..."
            textSize = 18f
            gravity  = Gravity.CENTER
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16f) }
        })

        val root = window.decorView.rootView as? android.widget.FrameLayout
        root?.addView(overlay)
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(this@PvpQuizActivity, "Không thể thoát khi đang đấu!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        WebSocketManager.onPvpProgressReceived = null
        WebSocketManager.onPvpResultReceived   = null
    }
}