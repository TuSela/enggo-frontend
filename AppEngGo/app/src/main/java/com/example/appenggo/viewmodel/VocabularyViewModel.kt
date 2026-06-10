package com.example.appenggo.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appenggo.Resource
import com.example.appenggo.SingleLiveEvent
import com.example.appenggo.model.RandomExamRequest
import com.example.appenggo.model.StartExamResponse
import com.example.appenggo.model.ThemeResponse
import com.example.appenggo.repository.ThemeRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VocabularyViewModel(
    private val repository: ThemeRepository
) : ViewModel() {

    val themes     = MutableLiveData<Resource<List<ThemeResponse>>>()
    val randomExam = SingleLiveEvent<Resource<StartExamResponse>>()

    fun fetchThemes(token: String) {
        viewModelScope.launch {
            themes.value = Resource.Loading()
            try {
                val response = repository.getAllThemes(token)
                val allThemes = response.result?.values?.flatten() ?: emptyList()
                themes.value = Resource.Success(allThemes)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("VocabularyVM", "fetchThemes HTTP ${e.code()}: $errorBody")
                themes.value = Resource.Error("HTTP ${e.code()}: $errorBody")
            } catch (e: Exception) {
                Log.e("VocabularyVM", "fetchThemes Error: ${e.message}")
                themes.value = Resource.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun getRandomExam(
        token: String,
        themeId: Int,
        difficulty: Int,
        totalQuestions: Int,
        questionTypes: List<String>
    ) {
        viewModelScope.launch {
            randomExam.value = Resource.Loading()
            try {
                // Bước 1: Tạo đề random → lấy examId
                val randomRequest = RandomExamRequest(
                    themeIds       = listOf(themeId),
                    difficulty     = difficulty,
                    totalQuestions = totalQuestions,
                    questionTypes  = questionTypes
                )
                Log.d("VocabularyVM", "Bước 1 - getRandomExam request: $randomRequest")
                val randomResponse = repository.getRandomExam(token, randomRequest)
                Log.d("VocabularyVM", "Bước 1 - getRandomExam response: ${randomResponse.result}")

                val examId = randomResponse.result?.id
                    ?: throw Exception("Không thể tạo đề thi")

                // Bước 2: Start exam → lấy attemptId + questions
                Log.d("VocabularyVM", "Bước 2 - startExam examId: $examId")
                val startResponse = repository.startExam(token, examId)
                Log.d("VocabularyVM", "Bước 2 - startExam response: ${startResponse.result}")

                val examData = startResponse.result
                    ?: throw Exception("Không thể bắt đầu bài thi")

                randomExam.value = Resource.Success(examData)

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("VocabularyVM", "HTTP ${e.code()}: $errorBody")
                randomExam.value = Resource.Error("HTTP ${e.code()}: $errorBody")
            } catch (e: Exception) {
                Log.e("VocabularyVM", "Error: ${e.message}", e)
                randomExam.value = Resource.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

}