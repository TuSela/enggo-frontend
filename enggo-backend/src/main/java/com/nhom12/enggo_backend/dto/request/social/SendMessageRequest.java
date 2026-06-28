package com.nhom12.enggo_backend.dto.request.social;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendMessageRequest {
    String content;
    String type; // "TEXT", "IMAGE", v.v. — để null thì mặc định "TEXT"
}