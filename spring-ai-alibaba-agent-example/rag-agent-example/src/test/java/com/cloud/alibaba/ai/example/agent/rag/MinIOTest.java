package com.cloud.alibaba.ai.example.agent.rag;


import com.cloud.alibaba.ai.example.agent.rag.Service.UploadService;
import io.minio.BucketExistsArgs;
import io.minio.ListBucketsArgs;
import io.minio.MinioClient;
import io.minio.messages.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

@SpringBootTest
public class MinIOTest {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void listBuckets() throws Exception {
        System.out.println(redisTemplate.opsForValue().get("listBucketsKey"));
    }

}
