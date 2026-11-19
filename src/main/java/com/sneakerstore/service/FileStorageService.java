package com.sneakerstore.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileStorageService {
    String uploadProductImage(MultipartFile file);
    InputStream getFileInputStream(String fileName);
    String getContentType(String fileName);
    void deleteFile(String fileName);
}
