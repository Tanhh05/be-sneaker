package com.sneakerstore.service.impl;

import com.sneakerstore.common.exception.BadRequestException;
import com.sneakerstore.service.FileStorageService;
import io.minio.*;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name:sneaker-products}")
    private String bucketName;

    @Value("${minio.public-url:http://localhost:9000/sneaker-products}")
    private String publicUrl;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml"
    );

    @PostConstruct
    public void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                log.info("MinIO bucket '{}' does not exist. Creating now...", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

                // Set public download policy
                String policy = """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": {"AWS": ["*"]},
                                    "Action": ["s3:GetObject"],
                                    "Resource": ["arn:aws:s3:::%s/*"]
                                }
                            ]
                        }
                        """.formatted(bucketName);
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
                log.info("MinIO bucket '{}' created with public download policy successfully.", bucketName);
            } else {
                log.info("MinIO bucket '{}' is ready.", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket '{}': {}", bucketName, e.getMessage());
        }
    }

    @Override
    public String uploadProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File tải lên không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Định dạng file không hợp lệ! Vui lòng chỉ tải ảnh (JPG, PNG, WEBP, GIF, SVG)");
        }

        try {
            String rawFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            // Sanitize filename
            String cleanName = rawFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String uniquePrefix = UUID.randomUUID().toString().substring(0, 8);
            String objectName = "product_" + timeStamp + "_" + uniquePrefix + "_" + cleanName;

            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(is, file.getSize(), -1)
                                .contentType(contentType)
                                .build()
                );
            }

            // Public MinIO URL
            String fileUrl = publicUrl.endsWith("/") ? publicUrl + objectName : publicUrl + "/" + objectName;
            log.info("Successfully uploaded image to MinIO: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("Failed to upload image to MinIO: {}", e.getMessage(), e);
            throw new BadRequestException("Lỗi lưu trữ ảnh vào MinIO: " + e.getMessage());
        }
    }

    @Override
    public InputStream getFileInputStream(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving file '{}' from MinIO: {}", fileName, e.getMessage());
            throw new BadRequestException("Không tìm thấy file trên MinIO: " + fileName);
        }
    }

    @Override
    public String getContentType(String fileName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return stat.contentType();
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("Deleted object '{}' from MinIO bucket '{}'", fileName, bucketName);
        } catch (Exception e) {
            log.error("Failed to delete object '{}' from MinIO: {}", fileName, e.getMessage());
        }
    }
}
