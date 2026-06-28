package com.nhom12.enggo_backend.controller.exam;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.exam.SkillRequest;
import com.nhom12.enggo_backend.entity.exam.Skill;
import com.nhom12.enggo_backend.repository.exam.SkillRepository;
import com.nhom12.enggo_backend.service.exam.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;

    @GetMapping("all")
    ApiResponse<List<Skill>> getAllSkills(
            @RequestParam(defaultValue = "skillName") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        return ApiResponse.<List<Skill>>builder()
                .result(skillService.getAllSkills(sortBy, direction))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<Skill> getSkill(@PathVariable("id") Integer id) {
        return ApiResponse.<Skill>builder()
                .result(skillService.getSkill(id))
                .build();
    }

    @PostMapping
    ApiResponse<Skill> addSkill(@RequestBody SkillRequest request) {
        return ApiResponse.<Skill>builder()
                .result(skillService.addSkill(request))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<Skill> updateSkill(@PathVariable("id") Integer id, @RequestBody SkillRequest request) {
        return ApiResponse.<Skill>builder()
                .result(skillService.updateSkill(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteSkill(@PathVariable("id") Integer id) {
        skillService.deleteSkill(id);
        return ApiResponse.<String>builder().result("Skill has been deleted").build();
    }
}
