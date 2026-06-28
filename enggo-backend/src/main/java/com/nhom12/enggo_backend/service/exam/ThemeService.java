package com.nhom12.enggo_backend.service.exam;

import com.nhom12.enggo_backend.dto.request.exam.ThemeRequest;
import com.nhom12.enggo_backend.dto.response.exam.ThemeResponse;
import com.nhom12.enggo_backend.entity.exam.Theme;
import com.nhom12.enggo_backend.mapper.exam.ThemeMapper;
import com.nhom12.enggo_backend.repository.exam.ThemeRepository;
import com.nhom12.enggo_backend.service.upload.UploadsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeMapper themeMapper;
    private final ThemeRepository themeRepository;
    private final UploadsService uploadsService;

    //Tao chu de
    public Theme addTheme(ThemeRequest request) throws IOException {
        if (themeRepository.existsByThemeName(request.getThemeName())) {
            throw new RuntimeException();
        }

        var theme = new Theme();
        theme.setThemeName(request.getThemeName());
        theme.setCategory(request.getCategory());
        theme.setThemeDescription(request.getThemeDescription());
        theme.setThemeImage(uploadsService.uploadImage(request.getThemeImage()));
        theme.setActive(true);
        theme.setCreatedAt(LocalDateTime.now().withNano(0));
        theme.setUpdatedAt(LocalDateTime.now().withNano(0));
        themeRepository.save(theme);

        return theme;
    }

    //Lay tat ca chu de theo tung phan loai
    public Map<String, List<ThemeResponse>> getAllThemesGroupedByCategory() {
        return themeRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Theme::getCategory,
                        Collectors.mapping(themeMapper::toThemeResponse, Collectors.toList())
                ));
    }

    //Lay tat ca chu de thuoc ve 1 phan loai nao do
    public Map<String, List<Theme>> getAllThemesByCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new RuntimeException("Category is required");
        }

        var trimmed = category.trim();

        if (!themeRepository.existsByCategory(trimmed)) {
            throw new RuntimeException("Category not found: " + trimmed);
        }

        var themes = themeRepository.findByCategory(trimmed);
        return themes.stream().collect(Collectors.groupingBy(Theme::getCategory));
    }

    public Theme getTheme(Integer id) {
        var theme = themeRepository.findById(id).orElseThrow(RuntimeException::new);
        return theme;
    }

    //cap nhat theme
    public Theme updateTheme(ThemeRequest request, Integer id) throws IOException {
        var theme = themeRepository.findById(id).orElseThrow(RuntimeException::new);

        if (!theme.getThemeName().equals(request.getThemeName())) {
            if (themeRepository.existsByThemeName(request.getThemeName())) {
                throw new RuntimeException();
            }
        }

        theme.setThemeName(request.getThemeName());
        theme.setCategory(request.getCategory());
        theme.setThemeDescription(request.getThemeDescription());

        if (request.getThemeImage() != null && !request.getThemeImage().isEmpty()) {
            theme.setThemeImage(uploadsService.uploadImage(request.getThemeImage()));
        }
        theme.setUpdatedAt(LocalDateTime.now().withNano(0));
        themeRepository.save(theme);
        return theme;
    }

    //Xoa theme
    public boolean deleteTheme(Integer id) {
        var theme = themeRepository.findById(id).orElseThrow(RuntimeException::new);
        themeRepository.delete(theme);
        return true;
    }

    public void enableTheme(Integer id) {
        var theme = themeRepository.findById(id).orElseThrow(RuntimeException::new);
        theme.setActive(true);
        themeRepository.save(theme);
    }

    public void disableTheme(Integer id) {
        var theme = themeRepository.findById(id).orElseThrow(RuntimeException::new);
        theme.setActive(false);
        themeRepository.save(theme);
    }
    public void uploadImageTheme(String url,Integer id){
        Theme theme = themeRepository.findById(id).orElseThrow(RuntimeException::new);
        theme.setThemeImage(url);
        themeRepository.save(theme);
    }
}
