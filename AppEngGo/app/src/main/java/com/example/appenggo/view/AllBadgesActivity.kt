package com.example.appenggo.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.BadgeAdapter
import com.example.appenggo.model.UserBadge
import com.example.appenggo.repository.UserRepository
import com.example.appenggo.viewmodel.ProfileResult
import com.example.appenggo.viewmodel.ProfileViewModel
import com.example.appenggo.viewmodel.ProfileViewModelFactory

class AllBadgesActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var badgeAdapter: BadgeAdapter
    private val badgeList = mutableListOf<UserBadge>()
    private lateinit var rvAllBadges: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_badges)

        rvAllBadges = findViewById(R.id.rv_all_badges)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupViewModel()
        observeViewModel()

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("TOKEN", "") ?: ""
        if (token.isNotEmpty()) {
            viewModel.fetchProfileData(token)
        }
    }

    private fun setupRecyclerView() {
        badgeAdapter = BadgeAdapter(badgeList, R.layout.item_badge_grid) { badge ->
            showBadgeDetailDialog(badge)
        }
        rvAllBadges.layoutManager = GridLayoutManager(this, 3)
        rvAllBadges.adapter = badgeAdapter
    }

    private fun showBadgeDetailDialog(badge: UserBadge) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_badge_detail)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val imgIcon: ImageView = dialog.findViewById(R.id.img_dialog_badge_icon)
        val tvName: TextView = dialog.findViewById(R.id.tv_dialog_badge_name)
        val tvDescription: TextView = dialog.findViewById(R.id.tv_dialog_badge_description)
        val btnClose: View = dialog.findViewById(R.id.btn_close_dialog)

        tvName.text = badge.badgeName
        tvDescription.text = badge.description

        val displayUrl = if (badge.iconUrl.contains(".svg")) {
            badge.iconUrl.replace(".svg", ".png")
        } else {
            badge.iconUrl
        }

        Glide.with(this)
            .load(displayUrl)
            .placeholder(R.drawable.ic_themes)
            .into(imgIcon)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupViewModel() {
        val repository = UserRepository(RetrofitClient.api)
        val factory = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.userBadges.observe(this) { result ->
            if (result is ProfileResult.Success) {
                badgeList.clear()
                result.data?.let {
                    badgeList.addAll(it)
                }
                badgeAdapter.notifyDataSetChanged()
            } else if (result is ProfileResult.Error) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
