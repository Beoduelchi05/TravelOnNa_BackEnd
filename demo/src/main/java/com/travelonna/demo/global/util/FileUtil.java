package com.travelonna.demo.global.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

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
     * 이미지가 너무 큰 경우에 리사이징합니다.
     * 
     * @param multipartFile 리사이징할 이미지 파일
     * @param maxDimension 최대 이미지 크기
     * @return 리사이징된 바이트 배열 또는 원본 바이트 배열
     */
    public static byte[] resizeImageIfNeeded(MultipartFile multipartFile, int maxDimension) throws IOException {
        try {
            BufferedImage originalImage = ImageIO.read(multipartFile.getInputStream());
            
            if (originalImage == null) {
                log.warn("Cannot read image file: {}", multipartFile.getOriginalFilename());
                return multipartFile.getBytes();
            }
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // 이미지가 최대 크기보다 크면 리사이징
            if (originalWidth > maxDimension || originalHeight > maxDimension) {
                log.info("Resizing image: original size {}x{}, max dimension: {}", 
                         originalWidth, originalHeight, maxDimension);
                
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                
                // 이미지 비율 유지하면서 리사이징
                if (originalWidth > originalHeight) {
                    Thumbnails.of(originalImage)
                        .width(maxDimension)
                        .keepAspectRatio(true)
                        .outputFormat(getImageFormat(multipartFile.getOriginalFilename()))
                        .toOutputStream(outputStream);
                } else {
                    Thumbnails.of(originalImage)
                        .height(maxDimension)
                        .keepAspectRatio(true)
                        .outputFormat(getImageFormat(multipartFile.getOriginalFilename()))
                        .toOutputStream(outputStream);
                }
                
                log.info("Image resized successfully");
                return outputStream.toByteArray();
            } else {
                log.info("Image size is within acceptable limits: {}x{}, no resizing needed", 
                         originalWidth, originalHeight);
                return multipartFile.getBytes();
            }
        } catch (Exception e) {
            log.error("Error during image resizing: {}", e.getMessage(), e);
            // 에러 발생 시 원본 이미지 반환
            return multipartFile.getBytes();
        }
    }
    
    /**
     * 프로필 이미지를 정사각형으로 크롭하고 리사이징합니다.
     * 
     * @param multipartFile 프로필 이미지 파일
     * @param size 정사각형 크기
     * @return 리사이징된 바이트 배열
     */
    public static byte[] cropAndResizeProfileImage(MultipartFile multipartFile, int size) throws IOException {
        try {
            BufferedImage originalImage = ImageIO.read(multipartFile.getInputStream());
            
            if (originalImage == null) {
                log.warn("Cannot read profile image file: {}", multipartFile.getOriginalFilename());
                return multipartFile.getBytes();
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // 이미지 중앙 부분을 정사각형으로 크롭하고 지정된 크기로 리사이징
            Thumbnails.of(originalImage)
                .size(size, size)
                .crop(Positions.CENTER)
                .outputFormat(getImageFormat(multipartFile.getOriginalFilename()))
                .toOutputStream(outputStream);
            
            log.info("Profile image resized to {}x{} successfully", size, size);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error during profile image processing: {}", e.getMessage(), e);
            // 에러 발생 시 원본 이미지 반환
            return multipartFile.getBytes();
        }
    }
    
    /**
     * 파일 이름에서 이미지 포맷을 추출합니다.
     * 
     * @param fileName 파일 이름
     * @return 이미지 포맷 (기본값: jpeg)
     */
    private static String getImageFormat(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        if (extension.isEmpty()) {
            return "jpeg";
        }
        
        // 확장자에서 점(.)을 제거
        extension = extension.substring(1);
        
        // 이미지 포맷 반환
        switch (extension) {
            case "jpg":
                return "jpeg";
            case "png":
            case "gif":
                return extension;
            default:
                return "jpeg";
        }
    }
} 