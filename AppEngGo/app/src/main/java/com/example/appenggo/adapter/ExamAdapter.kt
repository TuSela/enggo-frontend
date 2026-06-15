//package com.example.appenggo.adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.appenggo.R
//import com.example.appenggo.model.ExamItemResponse
//
//class ExamAdapter(
//    private var exams: List<ExamItemResponse>,
//    private val onExamClick: (ExamItemResponse) -> Unit
//) : RecyclerView.Adapter<ExamAdapter.ExamViewHolder>() {
//
//    inner class ExamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val tvTitle: TextView = view.findViewById(R.id.tv_exam_title)
//        val tvInfo: TextView = view.findViewById(R.id.tv_exam_info)
//        val tvDifficulty: TextView = view.findViewById(R.id.tv_exam_difficulty)
//
//        fun bind(exam: ExamItemResponse) {
//            tvTitle.text = exam.title
//            tvInfo.text = "Thời gian: ${exam.durationMinutes} phút | ${exam.totalQuestions} câu hỏi"
//            tvDifficulty.text = "Khó: ${exam.difficulty}"
//
//            itemView.setOnClickListener { onExamClick(exam) }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exam, parent, false)
//        return ExamViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
//        holder.bind(exams[position])
//    }
//
//    override fun getItemCount(): Int = exams.size
//
//    fun updateData(newExams: List<ExamItemResponse>) {
//        this.exams = newExams
//        notifyDataSetChanged()
//    }
//}