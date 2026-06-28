package com.nhom12.enggo_backend.service.exam;

import com.nhom12.enggo_backend.dto.request.exam.SkillRequest;
import com.nhom12.enggo_backend.entity.exam.Skill;
import com.nhom12.enggo_backend.mapper.exam.SkillMapper;
import com.nhom12.enggo_backend.repository.exam.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    //In ra 1 list skill theo
    public List<Skill> getAllSkills(String field, String direction) {
        List<String> allowedFields = List.of("skillName", "createAt");
        String sortField = allowedFields.contains(field) ? field : "createAt";

        Sort sort = direction.equalsIgnoreCase("ASC")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        return skillRepository.findAll(sort);
    }

    public Skill getSkill(Integer id) {
        var skill = skillRepository.findById(id).orElseThrow(RuntimeException::new);
        return skill;
    }

    public Skill addSkill(SkillRequest request) {
        if (skillRepository.existsBySkillName(request.getSkillName())) {
            throw new RuntimeException();
        }

        var skill = skillMapper.toSkill(request);
        skillRepository.save(skill);

        return skill;
    }

    public Skill updateSkill(Integer id, SkillRequest request) {
        var skill = skillRepository.findById(id).orElseThrow(RuntimeException::new);

        if (!skill.getSkillName().equals(request.getSkillName())) {
            if (skillRepository.existsBySkillName(request.getSkillName())) {
                throw new RuntimeException();
            }
        }

        skillMapper.updateSkill(skill, request);
        skill.setUpdatedAt(LocalDateTime.now().withNano(0));
        skillRepository.save(skill);
        return skill;
    }

    public boolean deleteSkill(Integer id) {
        var skill = skillRepository.findById(id).orElseThrow(RuntimeException::new);
        skillRepository.delete(skill);
        return true;
    }
}
