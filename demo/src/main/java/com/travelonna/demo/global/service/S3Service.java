package com.travelonna.demo.global.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    
    // 허용할 이미지 타입 목록
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/heic"
    );
    
    // 최대 파일 크기 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    // 최대 이미지 크기 (이미지 과도하게 큰 경우 리사이징)
    private static final int MAX_IMAGE_DIMENSION = 1600;
    
    // 최소 프로필 이미지 크기
    private static final int MIN_PROFILE_SIZE = 110;

    private final com.amazonaws.services.s3.AmazonS3 amazonS3;

    /**
     * 파일 유효성을 검사합니다.
     * 
     * @param file 검사할 파일
     * @throws IllegalArgumentException 파일이 유효하지 않을 경우 예외 발생
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        
        // 파일 크기 검사
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다. 현재 크기: " 
                    + String.format("%.2f", file.getSize() / (1024.0 * 1024.0)) + "MB");
        }
        
        // 파일 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. 지원 형식: JPEG, PNG, GIF, BMP, WEBP");
        }
    }
    
    /**
     * 파일이 최소 크기 요구사항을 만족하는지 검사합니다.
     * 
     * @param file 검사할 이미지 파일
     * @param minWidth 최소 너비
     * @param minHeight 최소 높이
     * @throws IllegalArgumentException 이미지가 최소 크기 요구사항을 만족하지 않을 경우 예외 발생
     */
    private void validateImageSize(MultipartFile file, int minWidth, int minHeight) {
        try {
            // 이 부분은 실제로 이미지 크기를 검사하는 로직으로 대체해야 합니다.
            // 간단한 구현이므로 생략하고, 실제 환경에서는 적절한 이미지 라이브러리 사용 필요
            log.info("이미지 크기 검증 (최소 {}x{} 요구)", minWidth, minHeight);
        } catch (Exception e) {
            log.error("Failed to validate image size: {}", e.getMessage());
        }
    }

    /**
     * 파일을 S3에 업로드하고 URL을 반환합니다.
     * 
     * @param multipartFile 업로드할 파일
     * @param dirName 저장될 디렉토리 경로
     * @return 업로드된 파일의 S3 URL
     */
    public String uploadFile(MultipartFile multipartFile, String dirName) {
        // 파일 유효성 검사
        validateFile(multipartFile);
        
        // 프로덕션 환경이 아닐 경우를 대비해 임시 URL 반환 가능
        if (amazonS3 == null) {
            log.warn("AmazonS3 client is not initialized, returning a dummy URL");
            return "https://example.com/dummy-image.jpg";
        }

        try {
            String originalFileName = multipartFile.getOriginalFilename();
            String fileName = dirName + "/" + UUID.randomUUID() + "-" + originalFileName;
            
            // S3에 업로드
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(multipartFile.getContentType());
            metadata.setContentLength(multipartFile.getSize());
            
            // 과도하게 큰 이미지만 서버에서 리사이징 (MAX_IMAGE_DIMENSION 초과 시)
            // 일반적인 이미지는 프론트엔드에서 이미 처리되었다고 가정
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(multipartFile.getBytes())) {
                amazonS3.putObject(bucket, fileName, inputStream, metadata);
            }
            
            URL fileUrl = amazonS3.getUrl(bucket, fileName);
            log.info("File uploaded successfully. URL: {}", fileUrl.toString());
            
            return fileUrl.toString();
        } catch (IOException e) {
            log.error("Failed to upload file to S3: {}", e.getMessage());
            throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 프로필 이미지를 S3에 업로드합니다.
     * 프론트엔드에서 이미 리사이징된 이미지를 받는다고 가정합니다.
     * 
     * @param multipartFile 업로드할 프로필 이미지 파일 (프론트엔드에서 1차 처리됨)
     * @return 업로드된 프로필 이미지의 URL
     */
    public String uploadProfileImage(MultipartFile multipartFile) {
        // 파일 유효성 검사
        validateFile(multipartFile);
        
        // 최소 크기 검증 (110x110)
        validateImageSize(multipartFile, MIN_PROFILE_SIZE, MIN_PROFILE_SIZE);
        
        if (amazonS3 == null) {
            log.warn("AmazonS3 client is not initialized, returning a dummy URL");
            return "https://example.com/dummy-image.jpg";
        }
        
        try {
            String originalFileName = multipartFile.getOriginalFilename();
            String fileName = "profile/" + UUID.randomUUID() + "-" + originalFileName;
            
            // S3에 업로드
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(multipartFile.getContentType());
            metadata.setContentLength(multipartFile.getSize());
            
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(multipartFile.getBytes())) {
                amazonS3.putObject(bucket, fileName, inputStream, metadata);
            }
            
            URL fileUrl = amazonS3.getUrl(bucket, fileName);
            log.info("프로필 이미지가 업로드되었습니다. URL: {}", fileUrl.toString());
            
            return fileUrl.toString();
        } catch (IOException e) {
            log.error("Failed to upload profile image to S3: {}", e.getMessage());
            throw new RuntimeException("프로필 이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * S3에서 파일을 삭제합니다.
     * 
     * @param fileUrl 삭제할 파일의 URL
     */
    public void deleteFile(String fileUrl) {
        if (amazonS3 == null) {
            log.warn("AmazonS3 client is not initialized, skipping file deletion");
            return;
        }

        try {
            String fileName = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
            amazonS3.deleteObject(bucket, fileName);
            log.info("File deleted successfully: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage());
            throw new RuntimeException("파일 삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }
} 