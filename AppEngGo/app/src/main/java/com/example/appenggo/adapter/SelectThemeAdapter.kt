package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.model.Entity.ThemeEntity

class SelectThemeAdapter(private val themes: List<ThemeEntity>) :
    RecyclerView.Adapter<SelectThemeAdapter.ViewHolder>() {

    private val selectedThemeIds = mutableSetOf<Int>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.cb_theme)
        val tvName: TextView = view.findViewById(R.id.tv_theme_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_theme, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val theme = themes[position]
        holder.tvName.text = theme.themeName
        
        // Gỡ listener trước khi set trạng thái để tránh trigger sai khi recycling view
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selectedThemeIds.contains(theme.id)

        holder.itemView.setOnClickListener {
            // Toggle checkbox sẽ kích hoạt listener bên dưới
            holder.checkBox.toggle()
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedThemeIds.add(theme.id)
            } else {
                selectedThemeIds.remove(theme.id)
            }
        }
    }

    override fun getItemCount() = themes.size

    fun getSelectedIds(): List<Int> = selectedThemeIds.toList()
}
