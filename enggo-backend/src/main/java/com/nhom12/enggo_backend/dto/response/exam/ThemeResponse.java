package com.nhom12.enggo_backend.dto.response.exam;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThemeResponse {
    Integer id;
    String themeName;
    String imageUrl;
}
