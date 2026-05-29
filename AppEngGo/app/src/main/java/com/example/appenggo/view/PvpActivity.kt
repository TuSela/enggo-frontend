package com.example.appenggo.view

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.appenggo.R

class PvpActivity : AppCompatActivity() {

    private lateinit var tabRanking: TextView
    private lateinit var tabInvite: TextView
    private lateinit var layoutRankingContent: LinearLayout
    private lateinit var layoutInviteContent: LinearLayout

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
        tabRanking.background = ContextCompat.getDrawable(this, R.drawable.btn_blue_fancy)
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.white))

        // Cập nhật Style cho Tab Mời Bạn (Inactive)
        tabInvite.background = ContextCompat.getDrawable(this, R.drawable.bg_card_light_blue)
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.black))
    }

    private fun showInviteTab() {
        // Hiện nội dung Mời bạn, ẩn nội dung Xếp hạng
        layoutInviteContent.visibility = View.VISIBLE
        layoutRankingContent.visibility = View.GONE

        // Cập nhật Style cho Tab Mời Bạn (Active)
        tabInvite.background = ContextCompat.getDrawable(this, R.drawable.btn_blue_fancy)
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.white))

        // Cập nhật Style cho Tab Xếp Hạng (Inactive)
        tabRanking.background = ContextCompat.getDrawable(this, R.drawable.bg_card_light_blue)
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.black))
    }
}