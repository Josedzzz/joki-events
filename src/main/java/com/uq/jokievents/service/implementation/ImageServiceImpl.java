package com.uq.jokievents.service.implementation;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.uq.jokievents.service.interfaces.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    @Override
    public String uploadImage(MultipartFile multipartFile) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket();
        String fileName = String.format("%s-s%s", UUID.randomUUID(), multipartFile.getOriginalFilename());
        Blob blob = bucket.create(fileName, multipartFile.getInputStream(), multipartFile.getContentType());

        return String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucket.getName(),
                blob.getName()
        );
    }

    @Override
    public void deleteImage(String imageName) {
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(imageName);
        blob.delete();
    }
}
