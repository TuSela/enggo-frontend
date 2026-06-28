package com.nhom12.enggo_backend.dto.response.social;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    Integer id;
    String name;
    String type;
    String lastMessageContent;
    LocalDateTime updatedAt;
}