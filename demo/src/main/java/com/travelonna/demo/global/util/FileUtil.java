package com.travelonna.demo.global.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {

    // 이미지 크기 검증을 위한 상수
    private static final int MAX_IMAGE_DIMENSION = 1600;
    private static final int MIN_PROFILE_SIZE = 110;

    /**
     * 임시 파일을 생성합니다.
     * S3 서비스가 사용할 수 없는 경우 로컬에 파일을 저장하기 위해 사용됩니다.
     *
     * @param multipartFile 변환할 MultipartFile
     * @return 생성된 임시 파일
     */
    public static File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        } catch (IOException e) {
            log.error("Failed to convert multipart file to file: {}", e.getMessage());
            throw e;
        }
        return file;
    }

    /**
     * 파일 확장자를 추출합니다.
     *
     * @param fileName 파일 이름
     * @return 파일 확장자
     */
    public static String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            log.warn("No file extension found for: {}", fileName);
            return "";
        }
    }

    /**
     * 고유한 파일 이름을 생성합니다.
     *
     * @param originalFileName 원본 파일 이름
     * @return UUID를 사용하여 생성된 고유한 파일 이름
     */
    public static String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID() + getFileExtension(originalFileName);
    }
    
    /**
     * 이미지가 너무 큰 경우에만 리사이징합니다.
     * 일반적으로 프론트엔드에서 처리한다고 가정하고, 백엔드에서는 지나치게 큰 이미지만 처리합니다.
     *
     * @param multipartFile 리사이징할 이미지 파일
     * @param maxDimension 최대 이미지 크기
     * @return 리사이징된 바이트 배열 또는 원본 바이트 배열
     */
    public static byte[] resizeImageIfNeeded(MultipartFile multipartFile, int maxDimension) throws IOException {
        // 실제 구현에서는 이미지 크기를 확인하고 필요 시 리사이징
        // 여기서는 예시로 항상 원본 반환
        return multipartFile.getBytes();
    }
} 