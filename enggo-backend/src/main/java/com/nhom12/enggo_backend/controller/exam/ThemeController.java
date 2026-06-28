package com.nhom12.enggo_backend.controller.exam;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.exam.ThemeRequest;
import com.nhom12.enggo_backend.dto.response.exam.ThemeResponse;
import com.nhom12.enggo_backend.entity.exam.Theme;
import com.nhom12.enggo_backend.service.exam.ThemeService;
import com.nhom12.enggo_backend.service.upload.UploadsService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeController {
    private final ThemeService themeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<Theme> addTheme(@ModelAttribute ThemeRequest request) throws IOException {
        return ApiResponse.<Theme>builder()
                .result(themeService.addTheme(request))
                .build();
    }

    //ttin mac dinh la 1 list cac theme dc tao ms nhat do xuong
    @GetMapping("/all")
    ApiResponse<Map<String, List<ThemeResponse>>> getAllThemesGroupedByCategory() {
        return ApiResponse.<Map<String, List<ThemeResponse>>>builder()
                .result(themeService.getAllThemesGroupedByCategory())
                .build();
    }

    @GetMapping("/category")
    ApiResponse<Map<String, List<Theme>>> getAllThemesByCategory(@RequestParam String category) {
        return ApiResponse.<Map<String, List<Theme>>>builder()
                .result(themeService.getAllThemesByCategory(category))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<Theme> getTheme(@PathVariable("id") Integer id) {
        return ApiResponse.<Theme>builder()
                .result(themeService.getTheme(id))
                .build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<Theme> updateTheme(
            @ModelAttribute ThemeRequest request,
            @PathVariable("id") Integer id) throws IOException {
        return ApiResponse.<Theme>builder()
                .result(themeService.updateTheme(request, id))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteTheme(@PathVariable("id") Integer id) {
        themeService.deleteTheme(id);
        return ApiResponse.<String>builder().result("Theme has been deleted").build();
    }

    @PutMapping("/{id}/enable")
    ApiResponse<String> enableTheme(@PathVariable("id")  Integer id) {
        themeService.enableTheme(id);
        return ApiResponse.<String>builder().result("Theme has been enabled").build();
    }

    @PutMapping("/{id}/disable")
    ApiResponse<String> disableTheme(@PathVariable("id")  Integer id) {
        themeService.disableTheme(id);
        return ApiResponse.<String>builder().result("Theme has been disabled").build();
    }
    private  final UploadsService uploadsService;
    @PutMapping("/{id}/uploadImage")
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file, @PathVariable("id")  Integer id) throws IOException {

        // 1. Upload ảnh lê
        // n Cloud
        String imageUrl = uploadsService.uploadImage(file);

        // 2. Cập nhật đường dẫn ảnh vào Database cho User tương ứng
        themeService.uploadImageTheme(imageUrl,id);

        return ApiResponse.<String>builder()
                .result(imageUrl)
                .message("Cập nhật ảnh đại diện thành công")
                .build();
    }
}
