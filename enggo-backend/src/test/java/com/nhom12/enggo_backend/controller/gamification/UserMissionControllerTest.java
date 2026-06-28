package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.response.gamification.ClaimRewardResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MissionProgressResponse;
import com.nhom12.enggo_backend.service.UserService;
import com.nhom12.enggo_backend.service.gamification.UserMissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserMissionController.class)
@WithMockUser(username = "testUser") // mock authentication
class UserMissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserMissionService userMissionService;

    @MockBean
    private UserService userService;

    private final Integer userId = 1;
    private final Integer missionId = 10;

    @BeforeEach
    void setUp() {
        // Mock getMyInfo to return a UserResponse with id = userId
        var userResponse = com.nhom12.enggo_backend.dto.response.UserResponse.builder()
                .id(userId)
                .username("testUser")
                .exp(100)
                .build();
        Mockito.when(userService.getMyInfo()).thenReturn(userResponse);
    }

    @Test
    void getTodayMissions_success() throws Exception {
        MissionProgressResponse progress = MissionProgressResponse.builder()
                .id(100)
                .userId(userId)
                .username("testUser")
                .missionId(missionId)
                .missionTitle("Test Mission")
                .currentValue(0)
                .status("IN_PROGRESS")
                .build();
        Mockito.when(userMissionService.getTodayMissions(userId))
                .thenReturn(List.of(progress));

        mockMvc.perform(get("/gamification/missions/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result[0].missionId").value(missionId));
    }

    @Test
    void incrementProgress_defaultIncrement() throws Exception {
        MissionProgressResponse updated = MissionProgressResponse.builder()
                .id(100).userId(userId).missionId(missionId).currentValue(1).status("IN_PROGRESS").build();
        Mockito.when(userMissionService.incrementProgress(userId, missionId, 1)).thenReturn(updated);

        mockMvc.perform(post("/gamification/missions/{missionId}/progress", missionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.currentValue").value(1));
    }

    @Test
    void incrementProgress_withBody() throws Exception {
        MissionProgressResponse updated = MissionProgressResponse.builder()
                .id(100).userId(userId).missionId(missionId).currentValue(3).status("IN_PROGRESS").build();
        Mockito.when(userMissionService.incrementProgress(userId, missionId, 2)).thenReturn(updated);
        Map<String, Integer> body = Collections.singletonMap("increment", 2);
        mockMvc.perform(post("/gamification/missions/{missionId}/progress", missionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.currentValue").value(3));
    }

    @Test
    void claimReward_success() throws Exception {
        ClaimRewardResponse claimResp = ClaimRewardResponse.builder()
                .expAwarded(20)
                .newTotalExp(120)
                .status("CLAIMED")
                .build();
        Mockito.when(userMissionService.claimReward(userId, missionId)).thenReturn(claimResp);
        mockMvc.perform(post("/gamification/missions/{missionId}/claim", missionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.expAwarded").value(20))
                .andExpect(jsonPath("$.result.status").value("CLAIMED"));
    }
}
