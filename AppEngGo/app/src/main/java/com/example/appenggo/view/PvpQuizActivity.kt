package com.example.appenggo.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.model.Request.*
import com.example.appenggo.model.Response.*
import com.example.appenggo.repository.PvpRepository
import com.example.appenggo.viewmodel.PvpViewModel
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable

class PvpQuizActivity : AppCompatActivity() {

    private val pvpRepository = PvpRepository()
    private val gson = Gson()
    private val compositeDisposable = CompositeDisposable()

    private lateinit var matchData: PvpMatchResponse
    private var currentQuestionIndex = 0
    private var myScore = 0
    private var opponentScore = 0
    private var myUserId: Int = -1
    private var matchId: Int = -1
    private var isSubmitting = false

    private val rawAnswers = mutableMapOf<Int, Any>()

    private lateinit var tvMyName: TextView
    private lateinit var tvOpponentName: TextView
    private lateinit var tvMyScoreDisplay: TextView
    private lateinit var tvOpponentScoreDisplay: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnNext: Button
    private lateinit var flContainer: FrameLayout
    private lateinit var pbBattle: ProgressBar

    private var countDownTimer: CountDownTimer? = null
    private var resultDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pvp_quiz)

        if (!parseIntentData()) {
            Toast.makeText(this, "Dữ liệu trận đấu không hợp lệ!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        ensureConnectionAndSubscribe()
        startTimer()
        displayQuestion()
    }

    private fun parseIntentData(): Boolean {
        return try {
            val json = intent.getStringExtra("MATCH_JSON") ?: return false
            matchData = gson.fromJson(json, PvpMatchResponse::class.java)
            matchId = intent.getIntExtra("MATCH_ID", -1)
            if (matchId == -1) matchId = matchData.matchId
            
            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            myUserId = sharedPref.getInt("USER_ID", -1)
            true
        } catch (e: Exception) {
            Log.e("PVP_QUIZ", "Lỗi parse Intent: ${e.message}")
            false
        }
    }

    private fun initViews() {
        tvMyName = findViewById(R.id.tv_my_name)
        tvOpponentName = findViewById(R.id.tv_opponent_name)
        tvMyScoreDisplay = findViewById(R.id.tv_my_score)
        tvOpponentScoreDisplay = findViewById(R.id.tv_opponent_score)
        tvTimer = findViewById(R.id.tv_timer)
        btnNext = findViewById(R.id.btn_next)
        flContainer = findViewById(R.id.fl_question_container)
        pbBattle = findViewById(R.id.pb_battle)

        val p1Name = matchData.player1Username
        val p2Name = matchData.player2Username

        // Fix smart cast: Use safe call or local variable
        val amIPlayer1 = p1Name?.contains("Tôi") == true || true 
        
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val myUsername = sharedPref.getString("USERNAME", "") ?: ""

        if (p1Name == myUsername) {
            tvMyName.text = "Bạn ($p1Name)"
            tvOpponentName.text = p2Name ?: "Đối thủ"
        } else {
            tvMyName.text = "Bạn ($p2Name)"
            tvOpponentName.text = p1Name ?: "Đối thủ"
        }
        
        btnNext.setOnClickListener { handleNextQuestion() }
    }

    private fun ensureConnectionAndSubscribe() {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("TOKEN", "") ?: ""
        
        val connectDisp = pvpRepository.connectWebSocket(token, {
            runOnUiThread { setupSubscriptions() }
        }, {
            Log.e("PVP_QUIZ", "Lỗi WebSocket: ${it.message}")
        })
        connectDisp?.let { compositeDisposable.add(it) }
    }

    private fun setupSubscriptions() {
        val progressDisp = pvpRepository.subscribeMatchProgress(matchId) { payload ->
            try {
                val progress = gson.fromJson(payload, MatchProgress::class.java)
                runOnUiThread {
                    if (progress.userId == myUserId) {
                        myScore = progress.currentScore
                        tvMyScoreDisplay.text = myScore.toString()
                    } else {
                        opponentScore = progress.currentScore
                        tvOpponentScoreDisplay.text = opponentScore.toString()
                    }
                    updateBattleProgress()
                }
            } catch (e: Exception) {
                Log.e("PVP_QUIZ", "Lỗi nhận tiến độ: ${e.message}")
            }
        }
        compositeDisposable.add(progressDisp)

        val resultDisp = pvpRepository.subscribeMatchResult(matchId) { payload ->
            try {
                val result = gson.fromJson(payload, MatchResultResponse::class.java)
                runOnUiThread { showResultDialog(result) }
            } catch (e: Exception) {
                Log.e("PVP_QUIZ", "Lỗi nhận kết quả: ${e.message}")
            }
        }
        compositeDisposable.add(resultDisp)
    }

    private fun displayQuestion() {
        val questions = matchData.questions
        if (questions.isNullOrEmpty()) return
        
        flContainer.removeAllViews()
        
        val question = questions[currentQuestionIndex].question
        when (question.questionType) {
            "MULTIPLE_CHOICE" -> renderMultipleChoice(question)
            "FILL_BLANK" -> renderFillBlank(question)
            "MATCHING" -> renderMatching(question)
        }
        btnNext.text = if (currentQuestionIndex == questions.size - 1) "NỘP BÀI" else "CÂU TIẾP THEO"
    }

    private fun renderMultipleChoice(question: PvpQuestion) {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_quiz_multiple, flContainer, false)
        val tvContent = view.findViewById<TextView>(R.id.tv_question_content)
        val rgOptions = view.findViewById<RadioGroup>(R.id.rg_options)

        tvContent.text = question.content
        question.multipleOptions?.forEach { option ->
            val rb = RadioButton(this).apply {
                text = option.optionText
                id = option.id
                textSize = 16f
                setPadding(20, 20, 20, 20)
            }
            rgOptions.addView(rb)
        }
        
        rgOptions.setOnCheckedChangeListener { _, checkedId ->
            rawAnswers[question.id] = checkedId
            sendCurrentProgress(question.id)
        }
        flContainer.addView(view)
    }

    private fun renderFillBlank(question: PvpQuestion) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        val tvContent = TextView(this).apply {
            text = question.content
            textSize = 18f
            setTextColor(Color.BLACK)
        }
        container.addView(tvContent)

        @Suppress("UNCHECKED_CAST")
        val answerMap = rawAnswers[question.id] as? MutableMap<Int, String> ?: mutableMapOf()

        question.fillBlankOptions?.forEach { option ->
            val et = EditText(this).apply {
                hint = "Đáp án..."
                setText(answerMap[option.blankId] ?: "")
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        answerMap[option.blankId] = s.toString()
                        rawAnswers[question.id] = answerMap
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
            container.addView(et)
        }
        flContainer.addView(container)
    }

    private fun renderMatching(question: PvpQuestion) {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_quiz_matching, flContainer, false)
        val llLeft = view.findViewById<LinearLayout>(R.id.ll_left_options)
        val llRight = view.findViewById<LinearLayout>(R.id.ll_right_options)

        var selectedLeft: Int? = null
        val leftButtons = mutableMapOf<Int, Button>()

        question.leftOptions?.forEach { opt ->
            val btn = Button(this).apply {
                text = opt.optionText
                isAllCaps = false
                setOnClickListener {
                    selectedLeft = opt.id
                    leftButtons.values.forEach { it.setBackgroundColor(Color.LTGRAY) }
                    setBackgroundColor(Color.YELLOW)
                }
            }
            leftButtons[opt.id] = btn
            llLeft.addView(btn)
        }

        question.rightOptions?.forEach { opt ->
            val btn = Button(this).apply {
                text = opt.optionText
                isAllCaps = false
                setOnClickListener {
                    val leftId = selectedLeft
                    if (leftId != null) {
                        @Suppress("UNCHECKED_CAST")
                        val currentMap = rawAnswers[question.id] as? MutableMap<Int, Int> ?: mutableMapOf()
                        currentMap[leftId] = opt.id
                        rawAnswers[question.id] = currentMap
                        
                        Toast.makeText(context, "Đã nối cặp!", Toast.LENGTH_SHORT).show()
                        sendCurrentProgress(question.id)
                        
                        selectedLeft = null
                        leftButtons.values.forEach { it.setBackgroundColor(Color.LTGRAY) }
                    }
                }
            }
            llRight.addView(btn)
        }
        flContainer.addView(view)
    }

    private fun sendCurrentProgress(questionId: Int) {
        val request = buildSingleAnswerRequest(questionId)
        pvpRepository.sendMatchProgress(matchId, gson.toJson(request))
    }

    private fun buildSingleAnswerRequest(questionId: Int): com.example.appenggo.model.Request.ExamAnswerRequest {
        val mapping = matchData.questions?.find { it.question.id == questionId }
        val question = mapping?.question ?: return com.example.appenggo.model.Request.ExamAnswerRequest(questionId)
        val userAnswer = rawAnswers[questionId]

        var selectedOptionId: Int? = null
        var fillBlanks: MutableList<FillBlankSubmitRequest>? = null
        var matchings: MutableList<MatchingSubmitRequest>? = null

        when (question.questionType) {
            "MULTIPLE_CHOICE" -> selectedOptionId = userAnswer as? Int
            "FILL_BLANK" -> {
                val map = userAnswer as? Map<Int, String>
                if (!map.isNullOrEmpty()) {
                    fillBlanks = map.map { (blankId, input) ->
                        val pos = question.fillBlankOptions?.find { it.blankId == blankId }?.position ?: 0
                        FillBlankSubmitRequest(blankId, pos, input)
                    }.toMutableList()
                }
            }
            "MATCHING" -> {
                val map = userAnswer as? Map<Int, Int>
                if (!map.isNullOrEmpty()) {
                    matchings = map.map { (l, r) -> MatchingSubmitRequest(l, r) }.toMutableList()
                }
            }
        }
        return com.example.appenggo.model.Request.ExamAnswerRequest(questionId, selectedOptionId, fillBlanks, matchings)
    }

    private fun handleNextQuestion() {
        val questions = matchData.questions ?: return
        
        val currentQuestion = questions[currentQuestionIndex].question
        if (currentQuestion.questionType == "FILL_BLANK") {
            sendCurrentProgress(currentQuestion.id)
        }

        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            displayQuestion()
        } else {
            submitQuiz()
        }
    }

    private fun submitQuiz() {
        if (isSubmitting) return
        isSubmitting = true

        countDownTimer?.cancel()
        lockUI()

        val questions = matchData.questions ?: return
        val answerRequests = questions.map { mapping -> buildSingleAnswerRequest(mapping.question.id) }
        val submitRequest = ExamSubmitRequest(answerRequests)
        
        pvpRepository.sendQuizSubmit(matchId, gson.toJson(submitRequest))
        Toast.makeText(this, "Đã nộp bài. Đang chờ đối thủ...", Toast.LENGTH_LONG).show()
    }

    private fun updateBattleProgress() {
        val total = myScore + opponentScore
        if (total > 0) {
            pbBattle.progress = (myScore.toFloat() / total * 100).toInt()
        }
    }

    private fun startTimer() {
        val durationMs = matchData.durationMinutes * 60 * 1000L
        countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val min = (millisUntilFinished / 1000) / 60
                val sec = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", min, sec)
            }
            override fun onFinish() { submitQuiz() }
        }.start()
    }

    private fun lockUI() {
        btnNext.isEnabled = false
        flContainer.alpha = 0.5f
    }

    private fun showResultDialog(result: MatchResultResponse) {
        if (isFinishing || resultDialog != null) return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pvp_result, null)
        
        val tvStatus = dialogView.findViewById<TextView>(R.id.tv_result_status)
        val status = when {
            result.winnerId == myUserId -> "VICTORY"
            result.winnerId == 0 -> "DRAW"
            else -> "DEFEAT"
        }
        tvStatus.text = status
        
        val winColor = Color.parseColor("#4CAF50")
        val loseColor = Color.parseColor("#F44336")
        
        tvStatus.setTextColor(when(status) {
            "VICTORY" -> winColor
            "DEFEAT" -> loseColor
            else -> Color.GRAY
        })

        // Xử lý thông tin hiển thị khớp với thứ tự tvMyName / tvOpponentName
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val myUsername = sharedPref.getString("USERNAME", "") ?: ""

        val p1Name = matchData.player1Username
        val p2Name = matchData.player2Username

        if (p1Name == myUsername) {
            // Tôi là player1
            setupPlayerResultUI(dialogView, true, p1Name, result.player1, winColor, loseColor)
            setupPlayerResultUI(dialogView, false, p2Name, result.player2, winColor, loseColor)
        } else {
            // Tôi là player2
            setupPlayerResultUI(dialogView, true, p2Name, result.player2, winColor, loseColor)
            setupPlayerResultUI(dialogView, false, p1Name, result.player1, winColor, loseColor)
        }

        resultDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btn_confirm_result).setOnClickListener {
            resultDialog?.dismiss()
            PvpViewModel.lastStartedMatchId = -1 
            val viewModel = ViewModelProvider(this)[PvpViewModel::class.java]
            viewModel.clearPvpQuiz()
            finish() 
        }
        resultDialog?.show()
    }

    private fun setupPlayerResultUI(
        dialogView: View,
        isMe: Boolean,
        username: String?,
        result: MatchResultResponse.PlayerResult,
        winColor: Int,
        loseColor: Int
    ) {
        val nameViewId = if (isMe) R.id.tv_p1_name else R.id.tv_p2_name
        val scoreViewId = if (isMe) R.id.tv_p1_score else R.id.tv_p2_score
        val correctViewId = if (isMe) R.id.tv_p1_correct else R.id.tv_p2_correct
        val eloViewId = if (isMe) R.id.tv_p1_elo_change else R.id.tv_p2_elo_change

        dialogView.findViewById<TextView>(nameViewId).text = if (isMe) "Bạn ($username)" else username
        dialogView.findViewById<TextView>(scoreViewId).text = "Điểm: ${result.playerScore}"
        dialogView.findViewById<TextView>(correctViewId).text = "Đúng: ${result.correctAnswersCount}/${matchData.totalQuestions}"
        
        dialogView.findViewById<TextView>(eloViewId).apply {
            text = "${if (result.eloChange >= 0) "+" else ""}${result.eloChange} Elo"
            setTextColor(if (result.eloChange >= 0) winColor else loseColor)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        countDownTimer?.cancel()
    }
}
