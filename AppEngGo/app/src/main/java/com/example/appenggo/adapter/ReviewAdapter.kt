package com.example.appenggo.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.model.ReviewQuestionWrapper

class ReviewAdapter(private val questions: List<ReviewQuestionWrapper>) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrder: TextView = view.findViewById(R.id.tv_order)
        val tvStatus: TextView = view.findViewById(R.id.tv_status_badge)
        val tvScore: TextView = view.findViewById(R.id.tv_score)
        val tvContent: TextView = view.findViewById(R.id.tv_question_content)
        val container: LinearLayout = view.findViewById(R.id.container_review_details)
        val layoutExp: View = view.findViewById(R.id.layout_explanation)
        val tvExp: TextView = view.findViewById(R.id.tv_explanation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review_question, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questions[position]
        val q = item.question

        holder.tvOrder.text = "${position + 1}"
        holder.tvContent.text = q.content
        holder.tvScore.text = if (item.score > 0) "+${item.score}" else "${item.score}"

        if (item.isCorrect) {
            holder.tvStatus.text = "ĐÚNG"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
        } else {
            holder.tvStatus.text = "SAI"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_red)
        }

        // Explanation
        if (!q.explanation.isNullOrBlank()) {
            holder.layoutExp.visibility = View.VISIBLE
            holder.tvExp.text = q.explanation
        } else {
            holder.layoutExp.visibility = View.GONE
        }

        holder.container.removeAllViews()
        val context = holder.itemView.context

        // Lấy font nunito_bold từ thư mục res/font
        val nunitoBold = ResourcesCompat.getFont(context, R.font.nunito_bold)

        when (q.questionType) {
            "MULTIPLE_CHOICE" -> {
                q.multipleOptions?.forEach { opt ->
                    val tv = TextView(context).apply {
                        text = opt.optionText
                        setPadding(0, 8, 0, 8)
                        textSize = 14f
                        typeface = nunitoBold // Set font chữ ở đây

                        if (opt.correct) {
                            setTextColor(Color.parseColor("#58CC02"))
                            text = "✓ $text"
                        } else if (opt.selected && !opt.correct) {
                            setTextColor(Color.RED)
                            text = "✗ $text"
                        } else {
                            setTextColor(Color.GRAY)
                        }
                    }
                    holder.container.addView(tv)
                }
            }
            "FILL_BLANK" -> {
                q.fillBlankOptions?.forEach { fb ->
                    val tv = TextView(context).apply {
                        val status = if (fb.isCorrect) "✓" else "✗"
                        text = "Ô ${fb.position}: ${fb.userInput ?: "(Trống)"} -> Đáp án: ${fb.correctValue} $status"
                        setTextColor(if (fb.isCorrect) Color.parseColor("#58CC02") else Color.RED)
                        setPadding(0, 8, 0, 8)
                        typeface = nunitoBold // Set font chữ ở đây
                    }
                    holder.container.addView(tv)
                }
            }
            "MATCHING" -> {
                q.matchingResults?.forEach { m ->
                    val tv = TextView(context).apply {
                        text = "${m.leftText} — ${m.userRightText ?: "?"} (Đúng: ${m.correctRightText})"
                        setTextColor(if (m.isCorrect) Color.parseColor("#58CC02") else Color.RED)
                        setPadding(0, 8, 0, 8)
                        typeface = nunitoBold // Set font chữ ở đây
                    }
                    holder.container.addView(tv)
                }
            }
        }
    }

    override fun getItemCount() = questions.size
}