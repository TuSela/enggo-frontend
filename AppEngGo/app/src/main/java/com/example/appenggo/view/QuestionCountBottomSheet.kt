package com.example.appenggo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.appenggo.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QuestionCountBottomSheet : BottomSheetDialogFragment() {

    var onCountSelected: ((Int) -> Unit)? = null

    private var currentCount: Int = 10

    companion object {
        const val TAG = "QuestionCountBottomSheet"
        private const val ARG_COUNT = "arg_count"
        const val MIN_COUNT = 5
        const val MAX_COUNT = 20
        const val STEP = 5

        fun newInstance(current: Int = 10): QuestionCountBottomSheet {
            return QuestionCountBottomSheet().apply {
                arguments = Bundle().apply { putInt(ARG_COUNT, current) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        currentCount = arguments?.getInt(ARG_COUNT, 10) ?: 10
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_question_count, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvCount    = view.findViewById<TextView>(R.id.tv_question_count)
        val btnMinus   = view.findViewById<View>(R.id.btn_minus)
        val btnPlus    = view.findViewById<View>(R.id.btn_plus)
        val btnConfirm = view.findViewById<View>(R.id.btn_confirm_question)
        tvCount.text = currentCount.toString()
        updateButtonStates(btnMinus, btnPlus)

        btnMinus.setOnClickListener {
            if (currentCount > MIN_COUNT) {
                currentCount -= STEP
                tvCount.text = currentCount.toString()
                updateButtonStates(btnMinus, btnPlus)
            }
        }

        btnPlus.setOnClickListener {
            if (currentCount < MAX_COUNT) {
                currentCount += STEP
                tvCount.text = currentCount.toString()
                updateButtonStates(btnMinus, btnPlus)
            }
        }

        btnConfirm.setOnClickListener {
            onCountSelected?.invoke(currentCount)
            dismiss()
        }

    }

    private fun updateButtonStates(btnMinus: View, btnPlus: View) {
        btnMinus.alpha = if (currentCount <= MIN_COUNT) 0.3f else 1.0f
        btnPlus.alpha  = if (currentCount >= MAX_COUNT) 0.3f else 1.0f
    }
}