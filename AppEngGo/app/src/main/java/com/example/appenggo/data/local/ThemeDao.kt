package com.example.appenggo.data.local

import androidx.room.*
import com.example.appenggo.model.Entity.ThemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeDao {
    @Query("SELECT * FROM themes")
    fun getAllThemes(): Flow<List<ThemeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThemes(themes: List<ThemeEntity>)

    @Query("DELETE FROM themes")
    suspend fun deleteAllThemes()
}
