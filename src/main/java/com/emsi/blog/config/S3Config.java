package com.emsi.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretKey:}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    private AwsCredentialsProvider getCredentialsProvider() {
        // If explicit credentials provided, use them (not recommended for production)
        if (accessKeyId != null && !accessKeyId.isEmpty() && 
            secretKey != null && !secretKey.isEmpty()) {
            return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretKey)
            );
        }
        // Otherwise use default credentials chain (AWS CLI, environment variables, IAM role)
        return DefaultCredentialsProvider.create();
    }
}
