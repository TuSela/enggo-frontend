package com.nhom12.enggo_backend.controller.exam;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.exam.QuestionCreationRequest;
import com.nhom12.enggo_backend.dto.request.exam.QuestionUpdateRequest;
import com.nhom12.enggo_backend.dto.response.PageResponse;
import com.nhom12.enggo_backend.dto.response.exam.QuestionResponse;
import com.nhom12.enggo_backend.dto.response.exam.QuestionDetailResponse;
import com.nhom12.enggo_backend.entity.exam.Question;
import com.nhom12.enggo_backend.service.exam.ExcelImportService;
import com.nhom12.enggo_backend.service.exam.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final ExcelImportService excelImportService;

    @PostMapping
    ApiResponse<QuestionDetailResponse> addQuestion(@RequestBody QuestionCreationRequest request) {
        return ApiResponse.<QuestionDetailResponse>builder()
                .result(questionService.addQuestion(request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteQuestion(@PathVariable Integer id) {
        questionService.deleteQuestion(id);
        return ApiResponse.<String>builder().result("deleted").build();
    }

    @GetMapping("/{id}")
    ApiResponse<QuestionDetailResponse> getQuestionById(@PathVariable("id") Integer id) {
        return ApiResponse.<QuestionDetailResponse>builder()
                .result(questionService.getQuestionById(id))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<PageResponse<QuestionResponse>> getAllQuestions(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<Integer> themeIds,
            @RequestParam(required = false) List<Integer> skillIds,
            @RequestParam(required = false) List<Byte> diffs
    ) {
        return ApiResponse.<PageResponse<QuestionResponse>>builder()
                .result(questionService.getAllQuestions(page, size, sortBy, direction, themeIds, skillIds, diffs))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<QuestionDetailResponse> updateQuestion(
            @PathVariable("id") Integer id,
            @RequestBody QuestionUpdateRequest request) {
        return ApiResponse.<QuestionDetailResponse>builder()
                .result(questionService.updateQuestion(id, request))
                .build();
    }

    @PostMapping("/import")
    ApiResponse<List<Question>> importQuestion(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.<List<Question>>builder()
                .result(excelImportService.importQuestions(file))
                .build();
    }
}
