package com.nhom12.enggo_backend.service.exam;

import com.nhom12.enggo_backend.dto.request.exam.OptionUpdateRequest;
import com.nhom12.enggo_backend.dto.request.exam.QuestionCreationRequest;
import com.nhom12.enggo_backend.dto.request.exam.QuestionUpdateRequest;
import com.nhom12.enggo_backend.dto.response.PageResponse;
import com.nhom12.enggo_backend.dto.response.exam.QuestionResponse;
import com.nhom12.enggo_backend.dto.response.exam.QuestionDetailResponse;
import com.nhom12.enggo_backend.entity.exam.*;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.mapper.UserMapper;
import com.nhom12.enggo_backend.mapper.exam.QuestionMapper;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.exam.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final OptionRepository optionRepository;
    private final ThemeRepository themeRepository;
    private final SkillRepository skillRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public QuestionDetailResponse addQuestion(QuestionCreationRequest request){
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByUsername(username).orElseThrow(RuntimeException::new);
        Question question = createQuestion(request,user);
        entityManager.flush();
        entityManager.clear();
        return questionMapper.toQuestionDetailResponse(questionRepository.findByIdWithTags(question.getId()).orElseThrow(RuntimeException::new));
    }

    @Transactional
    public Question createQuestion(QuestionCreationRequest request, User user){
        if (questionRepository.existsByContent(request.getContent())) {
            throw new RuntimeException("Question already exists");
        }

        var question = questionMapper.toQuestion(request);
        question.setCreatedBy(user);

        //Lay nhg request dap an cua question de tao entity option tuong ung
        List<QuestionOption> options = switch (request.getQuestionType()) {
            case "FILL_BLANK" -> questionMapper.mapFillBlankOption(request.getBlankAnswers(), question);
            case "MATCHING" -> questionMapper.mapMatchingQuestion(request.getOptions(), question);
            default -> questionMapper.toQuestionOptions(request.getOptions());
        };

        //Gan quan he question voi tung option
        for (QuestionOption option : options) {
            option.setQuestion(question);
        }
        question.setOptions(options);

        questionRepository.save(question);

        saveQuestionTag(question, request.getThemeIds(), request.getSkillIds());

        return question;
    }

    private void saveQuestionTag(Question question, List<Integer> themeIds, List<Integer> skillIds) {
        List<Theme> themes = themeRepository.findAllById(themeIds);
        List<Skill> skills = skillRepository.findAllById(skillIds);

        if (themes.size() != themeIds.size()) throw new RuntimeException("Theme and Theme Ids do not match");
        if (skills.size() != skillIds.size()) throw new RuntimeException("Skill Ids do not match");

        tagRepository.deleteByQuestionId(question.getId());

        List<QuestionTag> tags = new ArrayList<>();

        for (Theme theme : themes) {
            for (Skill skill : skills) {
                tags.add(QuestionTag.builder()
                        .id(new QuestionTagId(question.getId(), theme.getId(), skill.getId()))
                        .question(question)
                        .theme(theme)
                        .skill(skill)
                        .build()
                );
            }
        }
        //luu vao db
        tagRepository.saveAll(tags);
    }

    //Kiem tra xem List dap an co rong va co dap an dung ko
    private void validateOptions(List<QuestionOption> options){
        if (options == null || options.isEmpty()) {
            throw new RuntimeException("Option is required");
        }
        
        boolean hasCorrect = options.stream().anyMatch(QuestionOption::isCorrect);

        boolean hasOption = options.size() >= 2;

        if (!hasOption) {
            throw new RuntimeException("Answer has at least 2 options.");
        } else if (!hasCorrect) {
            throw new RuntimeException("Answer has at least 1 correct option.");
        }
    }

    public boolean deleteQuestion(Integer id) {
        var question = questionRepository.findById(id).orElseThrow(RuntimeException::new);
        questionRepository.delete(question);
        return true;
    }

    public QuestionDetailResponse getQuestionById(Integer id) {
        var question = questionRepository.findById(id).orElseThrow(RuntimeException::new);
        return questionMapper.toQuestionDetailResponse(question);
    }

    public PageResponse<QuestionResponse> getAllQuestions(
            int page,
            int size,
            String field,
            String direction,
            List<Integer> themeIds,
            List<Integer> skillIds,
            List<Byte> diffs
    ) {
        //Nhg field dc phep sap xep, neu ko thi mac dinh la createdAt
        List<String> allowedFields = List.of("content", "difficulty", "createdAt");
        String sortField = allowedFields.contains(field) ? field : "createdAt";

        Sort sort = direction.equalsIgnoreCase("ASC")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Integer> ids = questionRepository.findIdsByFilter(pageable,
                themeIds == null || themeIds.isEmpty() ? null : themeIds,
                skillIds == null || skillIds.isEmpty() ? null : skillIds,
                diffs == null || diffs.isEmpty() ? null : diffs);

        if (ids.isEmpty()) return PageResponse.of(new PageImpl<>(Collections.emptyList(), pageable, 0));

        List<Integer> sortedIds = ids.getContent();
        List<Question> content = questionRepository.findByIds(sortedIds);
        List<Question> strictSortedContent = content.stream()
                .sorted(Comparator.comparing(question -> sortedIds.indexOf(question.getId())))
                .toList();

        List<QuestionResponse> responses = questionMapper.toQuestionResponses(strictSortedContent);

        Page<QuestionResponse> responsePage = new PageImpl<>(responses, pageable, ids.getTotalElements());

        return PageResponse.of(responsePage);
    }

    @Transactional
    public QuestionDetailResponse updateQuestion(Integer id, QuestionUpdateRequest request){
        var question = questionRepository.findById(id).orElseThrow(RuntimeException::new);

        if (!question.getContent().equals(request.getContent())) {
            if (questionRepository.existsByContent(request.getContent())) {
                throw new RuntimeException();
            }
        }

        List<QuestionOption> options = questionMapper.toEntity(request.getOptions());

        questionMapper.updateQuestion(question, request);

        if (question.getOptions() != null) {
            updateQuestionOptions(question, request.getOptions());
        }

        validateOptions(options);
        question.setUpdatedAt(LocalDateTime.now().withNano(0));
        questionRepository.save(question);

        saveQuestionTag(question, request.getThemeIds(), request.getSkillIds());

        return questionMapper.toQuestionDetailResponse(questionRepository.findByIdWithTags(id).orElseThrow(RuntimeException::new));
    }

    @Transactional
    private void updateQuestionOptions (Question question, List<OptionUpdateRequest> requests) {
        //Tao 1 list cac option tu question cho vao
        List<QuestionOption> existOptions = question.getOptions();

        //Lay tu request nhung option ko null
        List<Integer> requestOptionIds = requests.stream()
                .map(OptionUpdateRequest::getId)
                .filter(Objects::nonNull)
                .toList();

        //Xoa cac option neu request ko con nx
        existOptions.removeIf(option -> !requestOptionIds.contains(option.getId()));

        //Voi moi request can update
        for (OptionUpdateRequest request : requests) {
            //Neu van con thi thay doi thong tin (lay id trung vs option da luu trg db roi thay doi ttin)
            if (request.getId() != null ) {
                existOptions.stream()
                        .filter(option -> option.getId().equals(request.getId()))
                        .findFirst()
                        .ifPresent(option -> questionMapper.updateOption(option, request));
            } else {
                //Neu co them option thi chi sang QuestionOption, luu thong tin cau hoi lien ket voi dap an
                var newOption = questionMapper.toEntity(request);
                newOption.setQuestion(question);
                existOptions.add(newOption);
            }
        }
    }
}
