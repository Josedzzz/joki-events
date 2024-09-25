package com.uq.jokievents.service.implementation;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.uq.jokievents.service.interfaces.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    @Override
    public String uploadImage(String base64Image) throws IOException {
        // Check if the input is valid
        if (base64Image == null || !base64Image.startsWith("data:image/")) {
            throw new IllegalArgumentException("Invalid Base64 image format.");
        }

        // Split the Base64 string to get the actual data
        String[] parts = base64Image.split(",");

        // Ensure we have the correct part containing the image data
        if (parts.length != 2) {
            throw new IllegalArgumentException("Base64 string is improperly formatted.");
        }

        String imageData = parts[1]; // This is the Base64 encoded data

        // Decode the Base64 string into a byte array
        byte[] imageBytes = Base64.getDecoder().decode(imageData);

        // Create a unique file name for the image
        String fileName = String.format("%s-s%s", UUID.randomUUID(), ".png"); // Change extension as needed

        // Get the bucket and upload the image
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.create(fileName, new ByteArrayInputStream(imageBytes), "image/png");

        // Return the public URL of the uploaded image
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
