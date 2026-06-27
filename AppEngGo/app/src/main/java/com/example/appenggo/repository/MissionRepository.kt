package com.example.appenggo.repository

import com.example.appenggo.ApiService
import com.example.appenggo.model.ClaimRewardResponse
import com.example.appenggo.model.MissionProgressResponse

class MissionRepository(private val apiService: ApiService) {

    suspend fun getTodayMissions(token: String): List<MissionProgressResponse> {
        val response = apiService.getTodayMissions("Bearer $token")
        return response.result ?: emptyList()
    }

    suspend fun claimReward(token: String, missionId: Int): ClaimRewardResponse {
        val response = apiService.claimMissionReward("Bearer $token", missionId)
        return response.result ?: throw Exception("Claim reward failed")
    }
}