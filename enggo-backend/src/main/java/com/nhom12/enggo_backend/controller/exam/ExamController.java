package com.nhom12.enggo_backend.controller.exam;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.exam.ExamSubmitRequest;
import com.nhom12.enggo_backend.dto.request.exam.RandomBlueprintRequest;
import com.nhom12.enggo_backend.dto.response.PageResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamDetailResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamDisplayResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamSubmitResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.exam.ExamAttemptRepository;
import com.nhom12.enggo_backend.repository.exam.ExamRepository;
import com.nhom12.enggo_backend.service.exam.ExamAttemptService;
import com.nhom12.enggo_backend.service.exam.ExamGenerationService;
import com.nhom12.enggo_backend.service.exam.ExamService;
import com.nhom12.enggo_backend.service.exam.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExcelImportService  excelImportService;
    private final ExamService examService;
    private final ExamAttemptService attemptService;
    private final ExamGenerationService examGenerationService;

    @PostMapping("/import")
    ApiResponse<ExamDetailResponse> importExamExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.<ExamDetailResponse>builder()
                .result(examService.createExam(file))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteExam(@PathVariable("id") Integer id){
        examService.deleteExam(id);
        return ApiResponse.<String>builder()
                .result("success")
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ExamDetailResponse> getExamDetail(@PathVariable("id")  Integer id){
        return ApiResponse.<ExamDetailResponse>builder()
                .result(examService.getExamDetail(id))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<PageResponse<ExamResponse>> getAllExams(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<Integer> themeIds,
            @RequestParam(required = false) List<Integer> skillIds,
            @RequestParam(required = false) List<Byte> diffs
    ) {
        return ApiResponse.<PageResponse<ExamResponse>>builder()
                .result(examService.getAllExams(page,size,sortBy,direction,themeIds,skillIds,diffs))
                .build();
    }

    @GetMapping("/{id}/start")
    ApiResponse<ExamDisplayResponse> startExam (@PathVariable("id") Integer id){
        return ApiResponse.<ExamDisplayResponse>builder()
                .result(examService.startExam(id))
                .build();
    }
    @PostMapping("/{examId}/attempt/{attemptId}/submit")
    ApiResponse<ExamSubmitResponse> submitExam (
            @RequestBody ExamSubmitRequest examSubmitRequest,
            @PathVariable("examId") Integer examId,
            @PathVariable("attemptId") Integer attemptId) {
        return ApiResponse.<ExamSubmitResponse>builder()
                .result(attemptService.submitExam(examSubmitRequest, examId, attemptId))
                .build();
    }

    @PostMapping("/random")
    public ApiResponse<ExamResponse> getOrCreateRandomExam (
            @RequestBody RandomBlueprintRequest request
            ) {
        return ApiResponse.<ExamResponse>builder()
                .result(examGenerationService.getOrGenerateExamResponse(request))
                .build();
    }
}
