package com.bsight.springserver.domain.business.service;

import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class BusinessLicenseFileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");

    @Value("${app.file.business-license-upload-dir:uploads/business-licenses}")
    private String uploadDir;

    public StoredBusinessLicenseFile store(MultipartFile file) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String extension = extractExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + "." + extension;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(storedFileName);
            file.transferTo(filePath.toFile());

            return new StoredBusinessLicenseFile(
                    originalFileName,
                    storedFileName,
                    filePath.toString(),
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_SAVE_FAILED);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_REQUIRED);
        }

        String originalFileName = file.getOriginalFilename();
        String extension = extractExtension(originalFileName);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        return originalFileName
                .substring(originalFileName.lastIndexOf(".") + 1)
                .toLowerCase();
    }

    public record StoredBusinessLicenseFile(
            String originalFileName,
            String storedFileName,
            String filePath,
            String contentType,
            Long fileSize
    ) {
    }
}