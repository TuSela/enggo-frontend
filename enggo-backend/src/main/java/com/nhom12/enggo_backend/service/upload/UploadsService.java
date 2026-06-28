package com.nhom12.enggo_backend.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UploadsService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_IMAGE_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/jpg", "image/svg+xml");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new AppException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        // Tạo Map động thay vì dùng ObjectUtils.asMap cố định
        Map<String, Object> params = new HashMap<>();
        params.put("resource_type", "auto"); // Để auto giúp Cloudinary tự bóc tách dòng byte tốt hơn

        // 🔥 CHÌA KHÓA XỬ LÝ: Nếu đúng là file SVG thì mới gợi ý ép format SVG
        if ("image/svg+xml".equals(contentType)) {
            params.put("format", "svg");
        }
        // Nếu là png/jpg thì KHÔNG PUT "format", Cloudinary sẽ tự giữ nguyên định dạng ảnh gốc!

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        // Trả về url dạng https để an toàn bảo mật khi gọi hiển thị trên iPhone/Android
        return uploadResult.get("secure_url") != null ?
                uploadResult.get("secure_url").toString() : uploadResult.get("url").toString();
    }
}