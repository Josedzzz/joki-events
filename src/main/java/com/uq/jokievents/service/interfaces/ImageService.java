package com.uq.jokievents.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {

    String uploadImage(String base64Image) throws IOException;
    void deleteImage(String imageName);
}
