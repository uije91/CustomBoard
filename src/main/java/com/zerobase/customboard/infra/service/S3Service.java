package com.zerobase.customboard.infra.service;

import static com.zerobase.customboard.global.exception.ErrorCode.INTERNAL_SERVER_ERROR;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.zerobase.customboard.global.exception.CustomException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.s3.baseUrl}")
  private String BASE_URL;


  public String uploadFile(MultipartFile multipartFile, String fileHeader) {

    String fileName = createFileName(fileHeader, multipartFile);

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(multipartFile.getSize());
    metadata.setContentType(multipartFile.getContentType());

    try {
      amazonS3.putObject(
          new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), metadata));
      log.info("[S3Service] : aws S3 파일 업로드 완료 : {}", fileName);
    } catch (IOException e) {
      throw new CustomException(INTERNAL_SERVER_ERROR);
    }
    return URLDecoder.decode(amazonS3.getUrl(bucket, fileName).toString(), StandardCharsets.UTF_8);
  }

  private String createFileName(String fileHeader, MultipartFile multipartFile) {
    return fileHeader + "/" + multipartFile.getOriginalFilename();
  }

  public void deleteFile(String uploadedFileName) {
    String key = uploadedFileName.replace(BASE_URL, "");

    try {
      amazonS3.deleteObject(bucket, key);
      log.info("[S3Service] : aws S3 파일 삭제 완료 : {}", key);
    } catch (AmazonServiceException e) {
      throw new AmazonServiceException(e.getErrorMessage());
    }
  }
}
