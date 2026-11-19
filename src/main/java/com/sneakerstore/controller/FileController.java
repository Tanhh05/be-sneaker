package com.sneakerstore.controller;

import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "09. File & MinIO Storage", description = "Quản lý tải tệp và lưu trữ ảnh sản phẩm lên MinIO")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải ảnh sản phẩm lên MinIO", description = "Chọn file từ máy và upload lưu trữ trực tiếp vào MinIO bucket sneaker-products")
    public ApiResponse<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileUrl = fileStorageService.uploadProductImage(file);
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("fileName", fileName);
        result.put("size", file.getSize());
        result.put("contentType", file.getContentType());
        result.put("originalName", file.getOriginalFilename());

        return ApiResponse.ok("Tải ảnh lên MinIO thành công", result);
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải nhiều ảnh lên MinIO")
    public ApiResponse<List<Map<String, Object>>> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String fileUrl = fileStorageService.uploadProductImage(file);
                String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
                Map<String, Object> map = new HashMap<>();
                map.put("url", fileUrl);
                map.put("fileName", fileName);
                map.put("originalName", file.getOriginalFilename());
                list.add(map);
            }
        }
        return ApiResponse.ok("Tải danh sách ảnh lên MinIO thành công", list);
    }

    @GetMapping("/view/{fileName}")
    @Operation(summary = "Xem trực tiếp ảnh từ MinIO qua Backend Proxy")
    public ResponseEntity<InputStreamResource> viewFile(@PathVariable String fileName) {
        InputStream is = fileStorageService.getFileInputStream(fileName);
        String contentType = fileStorageService.getContentType(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(new InputStreamResource(is));
    }
}
