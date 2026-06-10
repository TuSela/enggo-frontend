package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.model.ThemeResponse
import com.google.android.material.card.MaterialCardView

class ThemeAdapter(
    private var themes: List<ThemeResponse>,
    private val onThemeSelected: (ThemeResponse) -> Unit
) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {

    private var selectedPosition = -1

    inner class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardTheme: MaterialCardView = view.findViewById(R.id.card_theme)
        val tvThemeName: TextView = view.findViewById(R.id.tv_theme_name)

        fun bind(theme: ThemeResponse, position: Int) {
            tvThemeName.text = theme.themeName

            val ivCheck = itemView.findViewById<ImageView>(R.id.iv_check)

            if (selectedPosition == position) {
                cardTheme.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_pvp_setting_card_selected)
                cardTheme.strokeWidth = 0
                ivCheck.visibility = View.VISIBLE
            } else {
                cardTheme.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_pvp_setting_card)
                cardTheme.strokeWidth = 0
                ivCheck.visibility = View.GONE
            }

            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onThemeSelected(theme)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_theme, parent, false)
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        holder.bind(themes[position], position)
    }

    override fun getItemCount(): Int = themes.size

    fun updateData(newThemes: List<ThemeResponse>) {
        this.themes = newThemes
        notifyDataSetChanged()
    }
    fun getSelectedTheme(): ThemeResponse? {
        return if (selectedPosition >= 0) themes[selectedPosition] else null
    }

    fun setSelectedTheme(id: Int) {
        val index = themes.indexOfFirst { it.id == id }
        if (index >= 0) {
            selectedPosition = index
            notifyDataSetChanged()
        }
    }
}