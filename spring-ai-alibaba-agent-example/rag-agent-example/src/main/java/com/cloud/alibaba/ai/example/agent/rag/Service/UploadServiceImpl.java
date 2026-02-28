package com.cloud.alibaba.ai.example.agent.rag.Service;


import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author chenshimiao
 */

@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private MinioClient minioClient;


    @Override
    public void upload(MultipartFile file) throws IOException, ServerException,
            InsufficientDataException, ErrorResponseException,
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {

        String bucketName = "rag";

        // 检查存储桶是否存在，不存在则创建
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            System.out.println("存储桶 '" + bucketName + "' 已创建");
        }

        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucketName)
                .object("upload/" + file.getOriginalFilename())
                .contentType(file.getContentType())
                .stream(file.getInputStream(), file.getSize(), -1)
                .build();

        minioClient.putObject(args);

        // 发送kafka

        System.out.println("文件上传成功: " + file.getOriginalFilename());
    }
}
