package com.nhom12.enggo_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank
    @Size(min = 4, message = "USERNAME_INVALID")
    String username;

    @NotBlank
    @Email
    String email;

    @NotBlank
    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;
}
