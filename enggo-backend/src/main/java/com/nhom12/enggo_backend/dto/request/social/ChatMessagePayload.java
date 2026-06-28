package com.nhom12.enggo_backend.dto.request.social;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessagePayload {
    Integer conversationId;
    String content;
    String type; // "TEXT", "IMAGE", ...
}