package com.nhom12.enggo_backend.controller.exam;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.response.PageResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamSubmitResponse;
import com.nhom12.enggo_backend.entity.exam.ExamAttempt;
import com.nhom12.enggo_backend.service.exam.ExamAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attempts")
@RequiredArgsConstructor
public class ExamAttemptController {
    private final ExamAttemptService examAttemptService;

    @GetMapping("/{id}")
    public ApiResponse<ExamSubmitResponse> getAttemptById(@PathVariable("id") Integer id) {
        return ApiResponse.<ExamSubmitResponse>builder()
                .result(examAttemptService.getAttemptById(id))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<PageResponse<ExamSubmitResponse>> getAllAttempts(
            @RequestParam(defaultValue = "completedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<PageResponse<ExamSubmitResponse>>builder()
                .result(examAttemptService.getAllAttempts(page, size, sortBy, direction))
                .build();
    }
}
