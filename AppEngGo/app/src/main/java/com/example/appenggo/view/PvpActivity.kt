package com.example.appenggo.view

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.appenggo.R

class PvpActivity : AppCompatActivity() {

    private lateinit var tabRanking: TextView
    private lateinit var tabInvite: TextView
    private lateinit var layoutRankingContent: ScrollView
    private lateinit var layoutInviteContent: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pvp)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tabRanking = findViewById(R.id.tab_ranking)
        tabInvite = findViewById(R.id.tab_invite)
        layoutRankingContent = findViewById(R.id.layout_ranking_content)
        layoutInviteContent = findViewById(R.id.layout_invite_content)

        // Nút quay lại
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        tabRanking.setOnClickListener {
            showRankingTab()
        }

        tabInvite.setOnClickListener {
            showInviteTab()
        }
    }

    private fun showRankingTab() {
        // Hiện nội dung Xếp hạng, ẩn nội dung Mời bạn
        layoutRankingContent.visibility = View.VISIBLE
        layoutInviteContent.visibility = View.GONE

        // Cập nhật Style cho Tab Xếp Hạng (Active)
        tabRanking.background = ContextCompat.getDrawable(this, R.drawable.bg_pvp_tab_selected)
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))

        // Cập nhật Style cho Tab Mời Bạn (Inactive)
        tabInvite.background = null
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
    }

    private fun showInviteTab() {
        // Hiện nội dung Mời bạn, ẩn nội dung Xếp hạng
        layoutInviteContent.visibility = View.VISIBLE
        layoutRankingContent.visibility = View.GONE

        // Cập nhật Style cho Tab Mời Bạn (Active)
        tabInvite.background = ContextCompat.getDrawable(this, R.drawable.bg_pvp_tab_selected)
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))

        // Cập nhật Style cho Tab Xếp Hạng (Inactive)
        tabRanking.background = null
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
    }
}