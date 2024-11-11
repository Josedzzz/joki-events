package com.uq.jokievents.service.implementation;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.uq.jokievents.exceptions.LogicException;
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
    public String uploadImage(String base64Image){

        if (base64Image == null) throw new LogicException("base64Image is null");
        base64Image = base64Image.trim();

        // Check if the input is valid and extract image data
        ImageData imageData = extractImageData(base64Image);

        // Decode the Base64 string into a byte array
        byte[] imageBytes = Base64.getDecoder().decode(imageData.data);

        // Create a unique file name for the image
        String fileName = String.format("%s%s", UUID.randomUUID(), imageData.extension);

        // Get the bucket and upload the image
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.create(fileName, new ByteArrayInputStream(imageBytes), imageData.mimeType);

        // Return the public URL of the uploaded image
        return String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucket.getName(),
                blob.getName()
        );
    }

    // Helper method to parse the Base64 image and extract MIME type and image data
    private ImageData extractImageData(String base64Image) {
        if (!base64Image.startsWith("data:image/")) {
            throw new IllegalArgumentException("Invalid Base64 image format.");
        }

        // Extract the MIME type and actual Base64 encoded image data
        String[] parts = base64Image.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Base64 string is improperly formatted.");
        }

        String mimeType = parts[0].substring(5, parts[0].indexOf(";"));
        String data = parts[1];
        String extension = mimeTypeToExtension(mimeType);

        return new ImageData(mimeType, extension, data);
    }

    // Convert MIME type to file extension
    private String mimeTypeToExtension(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> ".jpeg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/bmp" -> ".bmp";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Unsupported image MIME type: " + mimeType);
        };
    }

    // Helper class to hold extracted image data
    private static class ImageData {
        String mimeType;
        String extension;
        String data;

        ImageData(String mimeType, String extension, String data) {
            this.mimeType = mimeType;
            this.extension = extension;
            this.data = data;
        }
    }

    @Override
    public void deleteImage(String imageName) {
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(imageName);
        blob.delete();
    }
}
