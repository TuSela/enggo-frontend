package com.example.appenggo.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import com.example.appenggo.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * DifficultyBottomSheet là một BottomSheetDialogFragment cho phép người dùng chọn mức độ khó cho bài kiểm tra hoặc trận đấu PVP.
 * Các mức độ bao gồm: Dễ, Vừa, Khó.
 */
class DifficultyBottomSheet : BottomSheetDialogFragment() {

    /**
     * Enum định nghĩa các mức độ khó và nhãn hiển thị tương ứng.
     */
    enum class Difficulty(val label: String) {
        EASY("Dễ"),
        MEDIUM("Vừa"),
        HARD("Khó")
    }

    /**
     * Callback được gọi khi người dùng xác nhận chọn một mức độ khó.
     */
    var onDifficultySelected: ((Difficulty) -> Unit)? = null

    private var selectedDifficulty: Difficulty = Difficulty.MEDIUM

    companion object {
        const val TAG = "DifficultyBottomSheet"
        private const val ARG_DIFFICULTY = "arg_difficulty"

        /**
         * Tạo một instance mới của DifficultyBottomSheet với độ khó hiện tại được truyền vào.
         * @param current Độ khó hiện tại để hiển thị trạng thái chọn ban đầu.
         */
        fun newInstance(current: Difficulty = Difficulty.MEDIUM): DifficultyBottomSheet {
            return DifficultyBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_DIFFICULTY, current.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Thiết lập style cho BottomSheet (bo tròn góc, background trong suốt)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)

        selectedDifficulty = arguments
            ?.getString(ARG_DIFFICULTY)
            ?.let { Difficulty.valueOf(it) }
            ?: Difficulty.MEDIUM
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_difficulty, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardEasy   = view.findViewById<RelativeLayout>(R.id.card_easy)
        val cardMedium = view.findViewById<RelativeLayout>(R.id.card_medium)
        val cardHard   = view.findViewById<RelativeLayout>(R.id.card_hard)
        val rbEasy     = view.findViewById<RadioButton>(R.id.rb_easy)
        val rbMedium   = view.findViewById<RadioButton>(R.id.rb_medium)
        val rbHard     = view.findViewById<RadioButton>(R.id.rb_hard)
        val btnConfirm = view.findViewById<View>(R.id.btn_confirm_difficulty)

        // Cập nhật trạng thái hiển thị ban đầu
        updateSelection(selectedDifficulty, cardEasy, cardMedium, cardHard, rbEasy, rbMedium, rbHard)

        // Thiết lập sự kiện click cho các thẻ (Card/RelativeLayout)
        cardEasy.setOnClickListener {
            selectedDifficulty = Difficulty.EASY
            updateSelection(selectedDifficulty, cardEasy, cardMedium, cardHard, rbEasy, rbMedium, rbHard)
        }
        cardMedium.setOnClickListener {
            selectedDifficulty = Difficulty.MEDIUM
            updateSelection(selectedDifficulty, cardEasy, cardMedium, cardHard, rbEasy, rbMedium, rbHard)
        }
        cardHard.setOnClickListener {
            selectedDifficulty = Difficulty.HARD
            updateSelection(selectedDifficulty, cardEasy, cardMedium, cardHard, rbEasy, rbMedium, rbHard)
        }

        // Đồng bộ sự kiện click từ RadioButton sang thẻ cha
        rbEasy.setOnClickListener   { cardEasy.performClick() }
        rbMedium.setOnClickListener { cardMedium.performClick() }
        rbHard.setOnClickListener   { cardHard.performClick() }

        // Nút xác nhận lựa chọn
        btnConfirm.setOnClickListener {
            onDifficultySelected?.invoke(selectedDifficulty)
            dismiss()
        }
    }

    /**
     * Cập nhật giao diện khi người dùng thay đổi lựa chọn.
     */
    private fun updateSelection(
        selected: Difficulty,
        cardEasy: RelativeLayout,
        cardMedium: RelativeLayout,
        cardHard: RelativeLayout,
        rbEasy: RadioButton,
        rbMedium: RadioButton,
        rbHard: RadioButton
    ) {
        val ctx = requireContext()
        val selectedBg = ContextCompat.getDrawable(ctx, R.drawable.bg_pvp_setting_card_selected)
        val normalBg   = ContextCompat.getDrawable(ctx, R.drawable.bg_pvp_setting_card)

        // Cập nhật background cho các thẻ
        cardEasy.background   = if (selected == Difficulty.EASY)   selectedBg else normalBg
        cardMedium.background = if (selected == Difficulty.MEDIUM) selectedBg else normalBg
        cardHard.background   = if (selected == Difficulty.HARD)   selectedBg else normalBg

        // Cập nhật trạng thái cho RadioButtons
        rbEasy.isChecked   = selected == Difficulty.EASY
        rbMedium.isChecked = selected == Difficulty.MEDIUM
        rbHard.isChecked   = selected == Difficulty.HARD

        // Ép màu: checked = cam, unchecked = xám — tránh Material3 override
        val radioTint = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(0xFFFF9600.toInt(), 0xFFAFAFAF.toInt())
        )
        rbEasy.buttonTintList   = radioTint
        rbMedium.buttonTintList = radioTint
        rbHard.buttonTintList   = radioTint
    }
}