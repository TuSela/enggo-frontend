package com.example.appenggo.model

import com.google.gson.annotations.SerializedName

/**
 * Request payload for creating a direct PVP match.
 * Mirrors the backend DTO `RandomBlueprintRequest`.
 */
data class RandomBlueprintRequest(
    /** Difficulty level (e.g., 1‑5). */
    @SerializedName("difficulty")
    val difficulty: Byte?,

    /** List of theme IDs to include in the exam. */
    @SerializedName("themeIds")
    val themeIds: List<Int>,

    /** Total number of questions for the exam. */
    @SerializedName("totalQuestions")
    val totalQuestions: Int,

    /** Types of questions, default matches backend defaults. */
    @SerializedName("questionTypes")
    val questionTypes: List<String> = listOf("MULTIPLE_CHOICE", "FILL_BLANK", "MATCHING")
)
