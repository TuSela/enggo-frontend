package com.example.appenggo.model.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey val id: Int,
    val themeName: String,
    val category: String? = null,
    val active: Boolean? = null
)