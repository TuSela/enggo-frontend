package com.example.appenggo.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
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

class ThemeSelectionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SELECTED_THEME_ID = "selected_theme_id"
        const val EXTRA_SELECTED_THEME_NAME = "selected_theme_name"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ThemeAdapter
    private lateinit var viewModel: VocabularyViewModel
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_selection)

        initViews()
        setupViewModel()
        
        val preSelectedId = intent.getIntExtra(EXTRA_SELECTED_THEME_ID, -1)
        setupRecyclerView(preSelectedId)
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

    private fun initViews() {
        progressBar = findViewById(R.id.progress_bar)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_confirm_topic)
            .setOnClickListener { confirmSelection() }
    }

    private fun setupViewModel() {
        val repository = ThemeRepository(RetrofitClient.api)
        val factory = VocabularyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[VocabularyViewModel::class.java]
    }

    private fun setupRecyclerView(preSelectedId: Int) {
        recyclerView = findViewById(R.id.rv_topics)
        adapter = ThemeAdapter(emptyList()) { /* optional live preview */ }
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.themes.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    resource.data?.let { themes ->
                        adapter.updateData(themes)
                        val preSelectedId = intent.getIntExtra(EXTRA_SELECTED_THEME_ID, -1)
                        if (preSelectedId != -1) {
                            adapter.setSelectedTheme(preSelectedId)
                        }
                    }
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmSelection() {
        val selected = adapter.getSelectedTheme() ?: run {
            Toast.makeText(this, "Hãy chọn một chủ đề!", Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent().apply {
            putExtra(EXTRA_SELECTED_THEME_ID, selected.id)
            putExtra(EXTRA_SELECTED_THEME_NAME, selected.themeName)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}