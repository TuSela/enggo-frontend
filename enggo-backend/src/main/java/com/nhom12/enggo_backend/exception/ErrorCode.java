package com.nhom12.enggo_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_OLD(1015,"mat khau cu khong khop",HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(1009, "Resource not found", HttpStatus.NOT_FOUND),
    RESOURCE_EXISTED(1010, "Resource existed", HttpStatus.BAD_REQUEST),
    BADGE_NAME_REQUIRED(1011, "Badge name is required", HttpStatus.BAD_REQUEST),
    BADGE_NAME_INVALID(1012, "Badge name must be at most 100 characters", HttpStatus.BAD_REQUEST),
    BADGE_REQUEST_INVALID(1013, "Badge request must be valid JSON", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1002, "File size exceeds the limit (Max: 5MB)", HttpStatus.PAYLOAD_TOO_LARGE),
    UNSUPPORTED_FILE_TYPE(1003, "Only JPEG, PNG, and WEBP are supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INVALID_OPERATION(1014, "Invalid operation", HttpStatus.BAD_REQUEST),
    FILE_EMPTY(1004, "Please select a file to upload", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(1005, "Cloudinary upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FRIEND_REQUEST_EXISTED(1011, "Lời mời kết bạn đã được gửi trước đó", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_NOT_FOUND(1012, "Không tìm thấy lời mời kết bạn hợp lệ", HttpStatus.NOT_FOUND),
    PASSWORD_FIELDS_REQUIRED(1012, "Yêu cầu nhập không được bỏ trống", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRM_NOT_MATCH(1013, "Your password does not match" , HttpStatus.BAD_REQUEST ),
    NEW_PASSWORD_SAME_AS_OLD(1013, "New password same as old" , HttpStatus.BAD_REQUEST ),;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
