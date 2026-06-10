package com.example.appenggo.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appenggo.Resource
import com.example.appenggo.model.*
import com.example.appenggo.repository.ThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuizViewModel(private val repository: ThemeRepository) : ViewModel() {

    private val _examData = MutableLiveData<Resource<StartExamResponse>>()
    val examData: LiveData<Resource<StartExamResponse>> = _examData

    private val _currentQuestionIndex = MutableLiveData<Int>(0)
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex

    private val _userAnswers = MutableLiveData<Map<Int, Any>>(mapOf())
    val userAnswers: LiveData<Map<Int, Any>> = _userAnswers

    private val _timeLeft = MutableLiveData<String>()
    val timeLeft: LiveData<String> = _timeLeft

    private val _submitResult = MutableLiveData<Resource<SubmitExamResponse>>()
    val submitResult: LiveData<Resource<SubmitExamResponse>> = _submitResult

    private var timer: CountDownTimer? = null
    private var isSubmitted = false

    fun loadExamData(examData: StartExamResponse) {
        _examData.value = Resource.Success(examData)
        startTimer(examData.durationMinutes)
    }

    fun startExam(token: String, examId: Int) {
        _examData.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.startExam(token, examId)
                }
                if (response.code == 1000 && response.result != null) {
                    _examData.value = Resource.Success(response.result)
                    startTimer(response.result.durationMinutes)
                } else {
                    _examData.value = Resource.Error(response.message ?: "Lỗi tải đề thi")
                }
            } catch (e: Exception) {
                _examData.value = Resource.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    private fun startTimer(minutes: Int) {
        timer?.cancel()
        timer = object : CountDownTimer((minutes * 60 * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val mins = (millisUntilFinished / 1000) / 60
                val secs = (millisUntilFinished / 1000) % 60
                _timeLeft.value = String.format("%02d:%02d", mins, secs)
            }
            override fun onFinish() {
                _timeLeft.value = "00:00"
            }
        }.start()
    }

    fun saveAnswer(questionId: Int, answer: Any) {
        val current = _userAnswers.value?.toMutableMap() ?: mutableMapOf()
        current[questionId] = answer
        _userAnswers.value = current
    }

    fun nextQuestion() {
        val current = _currentQuestionIndex.value ?: 0
        val total = (_examData.value?.data?.questions?.size ?: 0)
        if (current < total - 1) _currentQuestionIndex.value = current + 1
    }

    fun previousQuestion() {
        val current = _currentQuestionIndex.value ?: 0
        if (current > 0) _currentQuestionIndex.value = current - 1
    }

    fun submitExam(token: String) {
        if (isSubmitted) return
        val exam = _examData.value?.data ?: run {
            _submitResult.value = Resource.Error("Không có dữ liệu đề thi")
            return
        }

        isSubmitted = true
        timer?.cancel()

        val examId     = exam.examId
        val attemptId  = exam.attemptId
        val answersMap = _userAnswers.value ?: emptyMap()

        _submitResult.value = Resource.Loading()

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
                        fillBlanks = mutableListOf()
                        map.forEach { (blankId, input) ->
                            val pos = wrapper.question.fillBlankOptions
                                ?.find { it.blankId == blankId }?.position ?: 0
                            fillBlanks?.add(FillBlankAnswer(blankId, pos, input))
                        }
                    }
                }
                "MATCHING" -> {
                    val map = userAnswer as? Map<Int, Int>
                    if (!map.isNullOrEmpty()) {
                        matchings = mutableListOf()
                        map.forEach { (leftId, rightId) ->
                            matchings?.add(MatchingAnswer(leftId, rightId))
                        }
                    }
                }
            }
            ExamAnswerRequest(qId, selectedOptionId, fillBlanks, matchings)
        }

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.submitExam(token, examId, attemptId, SubmitExamRequest(answerRequests))
                }
                if (response.code == 1000 && response.result != null) {
                    _submitResult.value = Resource.Success(response.result)
                } else {
                    isSubmitted = false
                    _submitResult.value = Resource.Error(response.message ?: "Lỗi từ Server (${response.code})")
                }
            } catch (e: Exception) {
                isSubmitted = false
                _submitResult.value = Resource.Error("Lỗi hệ thống: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}