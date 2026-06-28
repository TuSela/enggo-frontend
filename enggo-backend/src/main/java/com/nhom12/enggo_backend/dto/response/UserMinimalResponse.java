package com.nhom12.enggo_backend.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMinimalResponse {
    private Integer id;
    private String username;
    private String avatarUrl;
    private Integer level;
    private String status;
}