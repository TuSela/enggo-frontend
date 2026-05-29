package com.example.appenggo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appenggo.Resource
import com.example.appenggo.model.ExamItemResponse
import com.example.appenggo.model.PageResponse
import com.example.appenggo.model.ThemeResponse
import com.example.appenggo.repository.ThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VocabularyViewModel(private val repository: ThemeRepository) : ViewModel() {

    private val _themes = MutableLiveData<Resource<List<ThemeResponse>>>()
    val themes: LiveData<Resource<List<ThemeResponse>>> = _themes

    private val _exams = MutableLiveData<Resource<PageResponse<ExamItemResponse>>>()
    val exams: LiveData<Resource<PageResponse<ExamItemResponse>>> = _exams

    fun fetchThemes(token: String) {
        _themes.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = repository.getAllThemes(formattedToken)
                if (response.code == 1000 && response.result != null) {
                    val allThemes = mutableListOf<ThemeResponse>()
                    response.result.values.forEach { allThemes.addAll(it) }
                    _themes.postValue(Resource.Success(allThemes))
                } else {
                    _themes.postValue(Resource.Error(response.message ?: "Lỗi tải chủ đề"))
                }
            } catch (e: Exception) {
                _themes.postValue(Resource.Error("Lỗi kết nối: ${e.message}"))
            }
        }
    }

    fun searchExams(token: String, themeId: Int, difficulty: Int) {
        _exams.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = repository.getExams(formattedToken, themeId, difficulty)
                if (response.code == 1000 && response.result != null) {
                    _exams.postValue(Resource.Success(response.result))
                } else {
                    _exams.postValue(Resource.Error(response.message ?: "Không tìm thấy đề thi"))
                }
            } catch (e: Exception) {
                _exams.postValue(Resource.Error("Lỗi kết nối: ${e.message}"))
            }
        }
    }
}