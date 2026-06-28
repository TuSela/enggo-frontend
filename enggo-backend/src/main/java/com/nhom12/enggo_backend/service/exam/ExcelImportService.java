package com.nhom12.enggo_backend.service.exam;

import com.nhom12.enggo_backend.entity.exam.*;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.exam.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExcelImportService {
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ThemeRepository themeRepository;
    private final SkillRepository skillRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    private String getCellString (Row row, int index) {
        Cell cell = row.getCell(index);
        if  (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    public Exam importExamExcel (MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Map<String, Exam> examMap = importExams(workbook.getSheet("exam"));
        Map<String, Question> questionMap = importQuestions(workbook.getSheet("question"));
        importOptions(workbook.getSheet("options"), questionMap);
        importExamQuestions(workbook.getSheet("exam_questions"), questionMap, examMap);
        importQuestionTags(workbook.getSheet("question_tags"), questionMap);

        return examMap.values().stream().findFirst().orElse(null);
    }

    public List<Question> importQuestions (MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Map<String, Question> questionMap = importQuestions(workbook.getSheet("question"));
        importOptions(workbook.getSheet("options"), questionMap);
        importQuestionTags(workbook.getSheet("question_tags"), questionMap);

        List<Question> questions = new ArrayList<>(questionMap.values());

        return questionRepository.saveAll(questions);
    }

    private Map<String, Exam> importExams (Sheet sheet) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByUsername(username).orElseThrow(RuntimeException::new);

        Map<String,Exam> examMap = new LinkedHashMap<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String rowId = getCellString(row, 0);

            Exam exam = Exam.builder()
                    .title(getCellString(row, 1))
                    .examType(getCellString(row, 2))
                    .difficulty((byte) row.getCell(3).getNumericCellValue())
                    .durationMinutes((int) row.getCell(4).getNumericCellValue())
                    .totalQuestions((int) row.getCell(5).getNumericCellValue())
                    .active(row.getCell(6).getBooleanCellValue())
                    .createdBy(user)
                    .expPerCorrectAnswer((int) row.getCell(7).getNumericCellValue())
                    .build();

            examRepository.save(exam);
            examMap.put(rowId, exam);
        }

        return examMap;
    }

    private Map<String, Question> importQuestions (Sheet sheet) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByUsername(username).orElseThrow(RuntimeException::new);

        Map<String,Question> questionMap = new LinkedHashMap<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String rowId = getCellString(row, 0);
            String existingId = getCellString(row, 1);

            Question question;

            if (existingId != null && !existingId.isBlank()) {
                int id = (int) Double.parseDouble(existingId);
                question = questionRepository.findById(id).orElseThrow(RuntimeException::new);
            } else {
                question = Question.builder()
                        .content(getCellString(row, 2))
                        .explanation(getCellString(row, 3))
                        .difficulty((byte) row.getCell(4).getNumericCellValue())
                        .attachmentUrl(getCellString(row, 5))
                        .createdBy(user)
                        .questionType(getCellString(row, 6))
                        .build();
                questionRepository.save(question);
            }
            questionMap.put(rowId, question);
        }
        return questionMap;
    }

    private void importOptions (Sheet sheet, Map<String, Question> questionMap) {
        List<QuestionOption> questionOptions = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String questionId = getCellString(row, 0);
            Question question = questionMap.get(questionId);
            if (question == null) continue;

            boolean isCorrect = false;
            Cell correctCell = row.getCell(2);
            if (correctCell != null) {
                String cellValue = getCellString(row, 2);
                assert cellValue != null;
                isCorrect = "TRUE".equalsIgnoreCase(cellValue.trim());
            }

            QuestionOption questionOption = QuestionOption.builder()
                    .question(question)
                    .optionText(getCellString(row, 1))
                    .correct(isCorrect)
                    .option_group(getCellString(row, 3))
                    .match_key(getCellString(row, 4))
                    .build();

            questionOptions.add(questionOption);
        }
        optionRepository.saveAll(questionOptions);
    }

    private void importExamQuestions (Sheet sheet, Map<String, Question> questionMap, Map<String, Exam> examMap) {
        List<ExamQuestion> examQuestions = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String examId = getCellString(row, 0);
            String questionId = getCellString(row, 1);

            Question question = questionMap.get(questionId);
            Exam exam = examMap.get(examId);

            if (exam == null || question == null) continue;

            ExamQuestion examQuestion = ExamQuestion.builder()
                    .id(new ExamQuestionId(question.getId(), exam.getId()))
                    .question(question)
                    .exam(exam)
                    .orderPriority((int)row.getCell(2).getNumericCellValue())
                    .build();

            examQuestions.add(examQuestion);
        }
        examQuestionRepository.saveAll(examQuestions);
    }

    private void importQuestionTags (Sheet sheet, Map<String, Question> questionMap) {
        List<QuestionTag> questionTags = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String[] themeNames = getCellString(row, 1).split(";");
            String[] skillNames = getCellString(row, 2).split(";");
            String questionId = getCellString(row, 0);

            Question question = questionMap.get(questionId);

            if (question == null) continue;

            for (String themeName: themeNames) {
                Theme theme = themeRepository.findByThemeName(themeName.trim());

                for (String skillName: skillNames) {
                    Skill skill = skillRepository.findBySkillName(skillName.trim());

                    questionTags.add(QuestionTag.builder()
                            .id(new QuestionTagId(question.getId(), theme.getId(), skill.getId()))
                            .theme(theme)
                            .skill(skill)
                            .question(question)
                            .build());
                }
            }

            tagRepository.saveAll(questionTags);
        }
    }
}
