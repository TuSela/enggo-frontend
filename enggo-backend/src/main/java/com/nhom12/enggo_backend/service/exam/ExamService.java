package com.nhom12.enggo_backend.service.exam;

import com.nhom12.enggo_backend.dto.response.PageResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamDetailResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamDisplayResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamQuestionResponse;
import com.nhom12.enggo_backend.dto.response.exam.ExamResponse;
import com.nhom12.enggo_backend.entity.exam.*;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.mapper.exam.ExamMapper;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.exam.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExcelImportService excelImportService;
    private final ThemeRepository themeRepository;
    private final SkillRepository skillRepository;
    private final ExamRepository examRepository;
    private final ExamTagRepository examTagRepository;
    private final TagRepository tagRepository;
    private final ExamMapper examMapper;
    private final QuestionRepository questionRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private final UserRepository userRepository;
    private final ExamAttemptRepository examAttemptRepository;

    private void saveExamTag(Exam exam, List<Integer> themeIds, List<Integer> skillIds){
        List<Theme> themes = themeRepository.findAllById(themeIds);
        List<Skill> skills = skillRepository.findAllById(skillIds);

        if (themes.size() != themeIds.size()) throw new RuntimeException();
        if (skills.size() != skillIds.size()) throw new RuntimeException();

        examTagRepository.deleteByExamId(exam.getId());

        List<ExamTag> examTags = new ArrayList<>();

        for (Theme theme : themes) {
            for (Skill skill : skills) {
                examTags.add(ExamTag.builder()
                        .id(new ExamTagId(exam.getId(), theme.getId(), skill.getId()))
                        .exam(exam)
                        .theme(theme)
                        .skill(skill)
                        .build()
                );
            }
        }
        examTagRepository.saveAll(examTags);
    }

    @Transactional
    public ExamDetailResponse createExam(MultipartFile file) throws IOException {
        Exam exam = excelImportService.importExamExcel(file);
        exam = examRepository.saveAndFlush(exam);
        Integer examId = exam.getId();

        List<Integer> themeIds = tagRepository.findDistinctThemeIdsByExamId(examId);
        List<Integer> skillIds = tagRepository.findDistinctSkillIdsByExamId(examId);

        saveExamTag(exam, themeIds, skillIds);
        entityManager.flush();
        entityManager.clear();
        Exam finalExam = examRepository.findByIdWithQuestions(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        examRepository.findByIdWithTags(examId);
        return toExamDetailResponse(finalExam);
    }

    public boolean deleteExam(Integer examId) {
        var exam = examRepository.findById(examId).orElseThrow(RuntimeException::new);
        examRepository.delete(exam);
        return true;
    }

    public ExamDetailResponse getExamDetail(Integer examId) {
        var exam = examRepository.findById(examId).orElseThrow(RuntimeException::new);
        return toExamDetailResponse(exam);
    }

    private ExamDetailResponse toExamDetailResponse(Exam exam) {
        var response = examMapper.toExamDetailResponse(exam);

        List<ExamQuestionResponse> questionResponses = exam.getExamQuestions().stream()
                .sorted(Comparator.comparing(ExamQuestion::getOrderPriority))
                .map(eq -> {
                    Question q = questionRepository.findByIdWithTags(eq.getQuestion().getId())
                            .orElseThrow(RuntimeException::new);
                    return examMapper.mapExamQuestion(
                            ExamQuestion.builder()
                                    .orderPriority(eq.getOrderPriority())
                                    .question(q)
                                    .exam(exam)
                                    .build()
                    );
                }).toList();

        response.setQuestions(questionResponses);
        return response;
    }
    public PageResponse<ExamResponse> getAllExams(
            int page,
            int size,
            String field,
            String direction,
            List<Integer> themeIds,
            List<Integer> skillIds,
            List<Byte> diffs) {
        List<String> allowFields = List.of("title", "difficulty", "createdAt");
        String sortField = allowFields.contains(field) ? field : "createdAt";

        Sort sort = direction.equalsIgnoreCase("ASC")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Integer> ids = examRepository.findIdsByFilter(pageable,
                themeIds == null || themeIds.isEmpty() ? null : themeIds,
                skillIds == null || skillIds.isEmpty() ? null : skillIds,
                diffs == null || diffs.isEmpty() ? null : diffs);

        if (ids.isEmpty()) return PageResponse.of(new PageImpl<>(Collections.emptyList(), pageable, 0));

        List<Integer> sortedIds = ids.getContent();
        List<Exam> content = examRepository.findByIds(sortedIds);
        List<Exam> strictSortedContent = content.stream()
                .sorted(Comparator.comparingInt(exam -> sortedIds.indexOf(exam.getId())))
                .toList();

        List<ExamResponse> responses = examMapper.toExamResponses(strictSortedContent);

        Page<ExamResponse> responsePage = new PageImpl<ExamResponse>(responses, pageable, ids.getTotalElements());

        return PageResponse.of(responsePage);
    }

    public ExamDisplayResponse startExam (Integer examId) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByUsername(username).orElseThrow(RuntimeException::new);

        var exam = examRepository.findById(examId).orElseThrow(RuntimeException::new);

        if (!exam.getActive()) {
            throw new IllegalStateException("Exam has been stopped");
        }

        boolean hasOngoing = examAttemptRepository.existsByUserIdAndCompleteFalse(user.getId());

        if (hasOngoing) {
            throw new IllegalStateException("You have an ongoing attempt");
        }
        ExamAttempt attempt = ExamAttempt.builder()
                .user(user)
                .exam(exam)
                .complete(false)
                .startedAt(LocalDateTime.now())
                .build();
        examAttemptRepository.save(attempt);
        return examMapper.toExamDisplayResponse(exam,attempt);
    }
}
