package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.appenggo.R

/**
 * PvpActivity quản lý màn hình chế độ chơi đối kháng (PVP).
 * Cho phép người dùng chọn chủ đề, độ khó và xem bảng xếp hạng hoặc lời mời thách đấu.
 */
class PvpActivity : AppCompatActivity() {

    private lateinit var tabRanking: TextView
    private lateinit var tabInvite: TextView
    private lateinit var layoutRankingContent: ScrollView
    private lateinit var layoutInviteContent: ScrollView

    private lateinit var cardTopic: LinearLayout
    private lateinit var tvTopicValue: TextView

    private lateinit var cardDifficulty: LinearLayout
    private lateinit var cardQuestionCount: LinearLayout
    private lateinit var tvQuestionCountValue: TextView
    private lateinit var tvDifficultyValue: TextView   // TextView hiển thị "Vừa" / "Dễ" / "Khó"
    private lateinit var ivDifficultyIcon: ImageView

    private var currentTopicId: Int = 1
    private var currentTopicName: String = "Gia đình"

    private var currentDifficulty: DifficultyBottomSheet.Difficulty = DifficultyBottomSheet.Difficulty.MEDIUM
    private var currentQuestionCount: Int = 10

    /**
     * Xử lý kết quả trả về từ màn hình chọn chủ đề (ThemeSelectionActivity).
     */
    private val topicLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val themeId = result.data?.getIntExtra(ThemeSelectionActivity.EXTRA_SELECTED_THEME_ID, -1) ?: -1
            val themeName = result.data?.getStringExtra(ThemeSelectionActivity.EXTRA_SELECTED_THEME_NAME)

            if (themeId != -1 && themeName != null) {
                currentTopicId = themeId
                currentTopicName = themeName
                tvTopicValue.text = themeName
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pvp)

        initViews()
        setupClickListeners()

        // Cập nhật giá trị ban đầu cho UI
        tvTopicValue.text = currentTopicName
        tvDifficultyValue.text = currentDifficulty.label
        updateSettingCardSelection(cardDifficulty)
    }

    /**
     * Khởi tạo các View từ layout.
     */
    private fun initViews() {
        tabRanking           = findViewById(R.id.tab_ranking)
        tabInvite            = findViewById(R.id.tab_invite)
        layoutRankingContent = findViewById(R.id.layout_ranking_content)
        layoutInviteContent  = findViewById(R.id.layout_invite_content)
        cardTopic            = findViewById(R.id.card_topic)
        tvTopicValue         = findViewById(R.id.tv_topic_value)
        cardDifficulty       = findViewById(R.id.card_difficulty)
        tvDifficultyValue    = findViewById(R.id.tv_difficulty_value)
        ivDifficultyIcon      = findViewById(R.id.iv_difficulty_icon)
        cardQuestionCount     = findViewById(R.id.card_question_count)
        tvQuestionCountValue  = findViewById(R.id.tv_question_count_value)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
    }

    /**
     * Thiết lập các sự kiện click cho các thành phần UI.
     */
    private fun setupClickListeners() {
        tabRanking.setOnClickListener { showRankingTab() }
        tabInvite.setOnClickListener  { showInviteTab() }
        cardTopic.setOnClickListener  { updateSettingCardSelection(cardTopic); openTopicSelection() }
        cardDifficulty.setOnClickListener { updateSettingCardSelection(cardDifficulty); openDifficultySelection() }
        cardQuestionCount.setOnClickListener { updateSettingCardSelection(cardQuestionCount); openQuestionCountSelection() }
    }

    /**
     * Hiển thị tab Bảng xếp hạng.
     */
    private fun showRankingTab() {
        layoutRankingContent.visibility = View.VISIBLE
        layoutInviteContent.visibility  = View.GONE
        tabRanking.background = ContextCompat.getDrawable(this, R.drawable.bg_pvp_tab_selected)
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        tabInvite.background = null
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
    }

    /**
     * Hiển thị tab Lời mời.
     */
    private fun showInviteTab() {
        layoutInviteContent.visibility  = View.VISIBLE
        layoutRankingContent.visibility = View.GONE
        tabInvite.background = ContextCompat.getDrawable(this, R.drawable.bg_pvp_tab_selected)
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        tabRanking.background = null
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
    }

    /**
     * Mở màn hình chọn chủ đề.
     */
    private fun openTopicSelection() {
        val intent = Intent(this, ThemeSelectionActivity::class.java).apply {
            putExtra(ThemeSelectionActivity.EXTRA_SELECTED_THEME_ID, currentTopicId)
        }
        topicLauncher.launch(intent)
    }

    /**
     * Hiển thị BottomSheet để người dùng chọn độ khó.
     */
    private fun openQuestionCountSelection() {
        val sheet = QuestionCountBottomSheet.newInstance(currentQuestionCount)
        sheet.onCountSelected = { count ->
            currentQuestionCount = count
            tvQuestionCountValue.text = "$count Câu"
        }
        sheet.show(supportFragmentManager, QuestionCountBottomSheet.TAG)
    }

    private fun updateSettingCardSelection(selected: LinearLayout) {
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_pvp_setting_card_selected)
        val normalBg   = ContextCompat.getDrawable(this, R.drawable.bg_pvp_setting_card)
        cardTopic.background         = if (selected == cardTopic)         selectedBg else normalBg
        cardDifficulty.background    = if (selected == cardDifficulty)    selectedBg else normalBg
        cardQuestionCount.background = if (selected == cardQuestionCount) selectedBg else normalBg
    }

    private fun openDifficultySelection() {
        val bottomSheet = DifficultyBottomSheet.newInstance(currentDifficulty)
        bottomSheet.onDifficultySelected = { difficulty ->
            currentDifficulty = difficulty
            tvDifficultyValue.text = difficulty.label
            val iconRes = when (difficulty) {
                DifficultyBottomSheet.Difficulty.EASY   -> R.drawable.ic_easy
                DifficultyBottomSheet.Difficulty.MEDIUM -> R.drawable.ic_medium
                DifficultyBottomSheet.Difficulty.HARD   -> R.drawable.ic_hard
            }
            ivDifficultyIcon.setImageResource(iconRes)
        }
        bottomSheet.show(supportFragmentManager, DifficultyBottomSheet.TAG)
    }
}