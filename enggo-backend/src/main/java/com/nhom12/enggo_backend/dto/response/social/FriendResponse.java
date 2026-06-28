package com.nhom12.enggo_backend.dto.response.social;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendResponse {
    Integer userId;
    String username;
    String avatarUrl;
    String bio;
    Integer level;
    boolean online;

    // true nếu status = "ONLINE"
}
