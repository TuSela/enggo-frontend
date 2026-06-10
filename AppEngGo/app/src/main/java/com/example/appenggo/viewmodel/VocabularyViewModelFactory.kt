package com.example.appenggo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.repository.ThemeRepository

class VocabularyViewModelFactory(
    private val repository: ThemeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(VocabularyViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                VocabularyViewModel(repository) as T
            }
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                QuizViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}