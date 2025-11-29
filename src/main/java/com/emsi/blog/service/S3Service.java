package com.emsi.blog.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return getFileUrl(fileName);
    }

    public String uploadProfilePicture(Integer userId, MultipartFile file) throws IOException {
        String fileName = "profile-pictures/" + userId + "/" + generateFileName(file.getOriginalFilename());
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return getFileUrl(fileName);
    }

    public void deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName != null && !fileName.isEmpty()) {
                deleteFile(fileName);
            }
        } catch (Exception e) {
            System.err.println("Error deleting file from S3: " + e.getMessage());
        }
    }

    public String getFileUrl(String fileName) {
        GetUrlRequest request = GetUrlRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        return s3Client.utilities().getUrl(request).toExternalForm();
    }

    private String generateFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "-" + originalFileName;
    }

    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        try {
            // Handle both s3.region.amazonaws.com and s3.amazonaws.com formats
            if (fileUrl.contains(".amazonaws.com/")) {
                int startIndex = fileUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length();
                return fileUrl.substring(startIndex);
            } else if (fileUrl.contains(bucketName + ".s3.")) {
                int startIndex = fileUrl.indexOf(bucketName) + bucketName.length() + 1;
                return fileUrl.substring(startIndex);
            }
        } catch (Exception e) {
            System.err.println("Error extracting filename from URL: " + e.getMessage());
        }
        return null;
    }
}
