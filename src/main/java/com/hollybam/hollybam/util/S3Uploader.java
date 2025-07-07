package com.hollybam.hollybam.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

    // 버킷 이름
    private final String bucket = "hollybam-static-image";

    public String upload(MultipartFile file, String dirName) throws IOException {
        String fileName = dirName + "/" + file.getOriginalFilename(); // 디렉토리/파일명
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);

        return amazonS3.getUrl(bucket, fileName).toString(); // S3 URL 반환
    }
}
