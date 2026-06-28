package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.exam.RandomBlueprintRequest;
import com.nhom12.enggo_backend.entity.exam.*;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.exam.ExamQuestionRepository;
import com.nhom12.enggo_backend.repository.exam.ExamRepository;
import com.nhom12.enggo_backend.repository.exam.ExamTagRepository;
import com.nhom12.enggo_backend.repository.exam.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamGenerationPVPService {
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamTagRepository examTagRepository;
    private final UserRepository userRepository;

    public List<Question> generateBalancedQuestions(RandomBlueprintRequest request) {
        //mac dinh co ca 3 loai cau hoi
        List<String> selectedTypes = request.getQuestionTypes();
        if (selectedTypes == null || selectedTypes.isEmpty()) {
            selectedTypes = List.of("MULTIPLE_CHOICE", "FILL_BLANK", "MATCHING");
        }

        //Lay cac cau hoi loc theo theme va type da chon
        List<Question> filteredPool = questionRepository.findQuestionsByThemesAndTypes(
                request.getThemeIds(), selectedTypes
        );

        //Tao map voi key la do kho
        Map<Byte, List<Question>> questionsByDifficulty = filteredPool.stream()
                .collect(Collectors.groupingBy(Question::getDifficulty));

        //Danh sach cac cau hoi theo do kho
        List<Question> easyPool = questionsByDifficulty.getOrDefault((byte) 1, new ArrayList<>());
        List<Question> mediumPool = questionsByDifficulty.getOrDefault((byte) 2, new ArrayList<>());
        List<Question> hardPool = questionsByDifficulty.getOrDefault((byte) 3, new ArrayList<>());

        //Tinh toan so cau hoi theo do kho
        int total = request.getTotalQuestions();
        int reqEasy, reqMedium, reqHard;

        if (total <= 5) {
            reqEasy   = (request.getDifficulty() == 1) ? total : 0;
            reqMedium = (request.getDifficulty() == 2) ? total : 0;
            reqHard   = (request.getDifficulty() == 3) ? total : 0;
        } else {
            // Nếu có chọn difficulty cụ thể (1, 2, 3) thì lấy toàn bộ theo mức đó
            if (request.getDifficulty() != null && request.getDifficulty() != 0) {
                reqEasy   = (request.getDifficulty() == 1) ? total : 0;
                reqMedium = (request.getDifficulty() == 2) ? total : 0;
                reqHard   = (request.getDifficulty() == 3) ? total : 0;
            } else {
                // Difficulty = 0 hoặc null = mixed → mới chia 50/30/20
                reqEasy   = (int) Math.round(total * 0.5);
                reqMedium = (int) Math.round(total * 0.3);
                reqHard   = total - reqEasy - reqMedium;
            }
        }

        log.info("Generation Stats | Req: [E:{}, M:{}, H:{}] | Pool: [E:{}, M:{}, H:{}]",
                reqEasy, reqMedium, reqHard,
                easyPool.size(), mediumPool.size(), hardPool.size());

        //Tong hop tat ca thanh 1 danh sach
        List<Question> finalSelected = new ArrayList<>();
        finalSelected.addAll(pickBalancedTypesAndThemes(easyPool, reqEasy, request.getThemeIds(), selectedTypes));
        finalSelected.addAll(pickBalancedTypesAndThemes(mediumPool, reqMedium, request.getThemeIds(), selectedTypes));
        finalSelected.addAll(pickBalancedTypesAndThemes(hardPool, reqHard, request.getThemeIds(), selectedTypes));

        //Kiem tra co du cau hoi ko
        if (finalSelected.size() < total) {
            throw new RuntimeException("Ngân hàng câu hỏi không đủ số lượng câu thỏa mãn cấu trúc đề chuẩn!");
        }

        //Tron trc khi tra ve (chang can thiet lam)
        Collections.shuffle(finalSelected);
        return finalSelected;
    }

    /**
     * Thuật toán vòng xoay luân phiên (Round-Robin):
     * Bảo đảm chia bài đan xen phẳng và đều giữa các Dạng câu hỏi (Type) VÀ các Chủ đề (Theme) đã chọn.
     */
    private List<Question> pickBalancedTypesAndThemes(
            List<Question> pool,
            int requiredCount,
            List<Integer> selectedThemeIds,
            List<String> allowedTypes) {

        List<Question> result = new ArrayList<>();
        if (pool.isEmpty() || requiredCount <= 0) return result;

        // 1. Xáo trộn kho câu hỏi gốc
        Collections.shuffle(pool);

        // 2. Xáo trộn danh sách Type để thay đổi thứ tự ưu tiên nhặt dạng bài
        List<String> shuffledTypes = new ArrayList<>(allowedTypes);
        Collections.shuffle(shuffledTypes);

        // 3. XÁO TRỘN CẢ THEME: Đây là chìa khóa để Theme 4 và Theme 5 có cơ hội đứng đầu như nhau
        List<Integer> shuffledThemes = new ArrayList<>(selectedThemeIds);
        Collections.shuffle(shuffledThemes);

        int safetyCheck = 0;
        outerWhile:
        while (result.size() < requiredCount && safetyCheck < pool.size() * 2) {
            safetyCheck++;

            // Chạy xoay vòng qua từng dạng bài
            for (String type : shuffledTypes) {
                // Chạy xoay vòng qua từng chủ đề
                for (Integer themeId : shuffledThemes) {

                    // Lọc câu hỏi khớp đồng thời cả Type, Theme và chưa từng được chọn
                    Optional<Question> matchQuestion = pool.stream()
                            .filter(q -> q.getQuestionType().equals(type)
                                    && q.getTags() != null
                                    && q.getTags().stream().anyMatch(t -> t.getTheme() != null && t.getTheme().getId().equals(themeId))
                                    && !result.contains(q))
                            .findFirst();

                    if (matchQuestion.isPresent()) {
                        result.add(matchQuestion.get());

                        // Kiểm tra nếu đủ tổng số câu yêu cầu cho giỏ này thì dừng hẳn
                        if (result.size() >= requiredCount) {
                            break outerWhile;
                        }

                        // CHÌA KHÓA CÂN BẰNG: Nhặt được 1 câu của cặp (Type, Theme) này rồi,
                        // lập tức bẻ gãy vòng lặp để chuyển sang Type kế tiếp và Theme kế tiếp ở lượt sau!
                        break;
                    }
                }
            }
        }

        // Bước đệm cứu hộ (Fallback): Nhặt vét nếu kho quá ngặt nghèo không đủ câu theo bộ lọc
        if (result.size() < requiredCount) {
            for (Question q : pool) {
                if (result.size() >= requiredCount) break;
                if (!result.contains(q)) {
                    result.add(q);
                }
            }
        }

        return result;
    }

    @Transactional
    public Exam getOrGenerateExamResponse(RandomBlueprintRequest request, User player) {
        User currentUser = player;
        Integer userId = currentUser != null ? currentUser.getId() : null;

        int totalQuestions = request.getTotalQuestions();
        if (totalQuestions != 5 && totalQuestions != 10 && totalQuestions != 15 && totalQuestions != 20) {
            totalQuestions = 20;
        }

        // =========================================================================
        // TẦNG 1: LUÔN LUÔN ƯU TIÊN TẠO ĐỀ MỚI TINH TRÊN RAM
        // =========================================================================
        try {
            List<Question> finalSelectedQuestions = generateBalancedQuestions(request);

            // Tiến hành đóng gói và lưu đề mới tinh này xuống DB
            String examCode = String.valueOf(System.currentTimeMillis() % 1000000);
            Exam dynamicExam = Exam.builder()
                    .title(String.format("Đề Tự Động %d Câu - Mã %s", totalQuestions, examCode))
                    .examType("DYNAMIC")
                    .difficulty(request.getDifficulty())
                    .totalQuestions(totalQuestions)
                    .durationMinutes(totalQuestions * 2)
                    .active(true)
                    .createdBy(currentUser)
                    .expPerCorrectAnswer(20)
                    .build();

            Exam savedExam = examRepository.save(dynamicExam);

            // --- Phần lưu examQuestions và examTags của bạn ---
            List<ExamQuestion> examQuestions = new ArrayList<>();
            for (int i = 0; i < finalSelectedQuestions.size(); i++) {
                ExamQuestionId eqId = new ExamQuestionId(savedExam.getId(), finalSelectedQuestions.get(i).getId());
                ExamQuestion eq = ExamQuestion.builder()
                        .id(eqId)
                        .exam(savedExam)
                        .question(finalSelectedQuestions.get(i))
                        .orderPriority(i + 1)
                        .build();
                examQuestions.add(eq);
            }
            examQuestionRepository.saveAll(examQuestions);

            Map<String, ExamTag> uniqueExamTagMap = new HashMap<>();
            for (Question q : finalSelectedQuestions) {
                if (q.getTags() == null) continue;
                for (QuestionTag tag : q.getTags()) {
                    if (tag.getTheme() != null && tag.getSkill() != null) {
                        Integer themeId = tag.getTheme().getId();
                        Integer skillId = tag.getSkill().getId();
                        String key = savedExam.getId() + "_" + themeId + "_" + skillId;

                        if (!uniqueExamTagMap.containsKey(key)) {
                            uniqueExamTagMap.put(key, ExamTag.builder()
                                    .id(new ExamTagId(savedExam.getId(), themeId, skillId))
                                    .exam(savedExam)
                                    .theme(tag.getTheme())
                                    .skill(tag.getSkill())
                                    .build());
                        }
                    }
                }
            }
            List<ExamTag> examTags = new ArrayList<>(uniqueExamTagMap.values());
            savedExam.setExamTags(examTags);

            return savedExam;

        } catch (RuntimeException e) {
            // =========================================================================
            // CƠ CHẾ CỨU HỘ KHI KHÔNG THỂ TẠO ĐỀ MỚI (Lỗi thiếu câu hỏi lẻ dưới DB)
            // =========================================================================

            // TẦNG 2: Tìm xem có đề cũ nào khớp cấu hình request mà user CHƯA TỪNG LÀM không
            if (userId != null) {
                List<Exam> unattemptedExams = examRepository.findAvailableExamsByThemesAndType(
                        request.getThemeIds(), totalQuestions, request.getQuestionTypes(), userId
                );
                if (!unattemptedExams.isEmpty()) {
                    int randomIndex = java.util.concurrent.ThreadLocalRandom.current().nextInt(unattemptedExams.size());
                    return unattemptedExams.get(randomIndex);
                }
            }

            // TẦNG 3: Nếu đã làm hết sạch các đề cũ -> Chấp nhận bốc đề cũ BẤT KỲ (kể cả đã làm)
            List<Exam> anyExams = examRepository.findAnyAvailableExams(
                    request.getThemeIds(), totalQuestions, request.getQuestionTypes()
            );

            if (!anyExams.isEmpty()) {
                int randomIndex = java.util.concurrent.ThreadLocalRandom.current().nextInt(anyExams.size());
                return anyExams.get(randomIndex);
            }

            // TẦNG 4: CỨU HỘ CUỐI CÙNG - Nếu không tìm thấy đề nào theo tiêu chí, bốc đại 1 đề ngẫu nhiên
            log.warn("No matching exams found for request. Falling back to a random exam.");
            return examRepository.findRandomExam()
                    .orElseThrow(() -> new RuntimeException("Ngân hàng câu hỏi trống rỗng, không tìm thấy bất kỳ đề thi nào!"));
        }
    }
}
