package com.cloud.alibaba.ai.example.agent.rag.Service;


import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;
import com.cloud.alibaba.ai.example.agent.rag.controller.UploadController;
import com.cloud.alibaba.ai.example.agent.rag.entity.ChunkInfo;
import com.cloud.alibaba.ai.example.agent.rag.entity.FileUpload;
import com.cloud.alibaba.ai.example.agent.rag.repository.ChunkInfoRepository;
import com.cloud.alibaba.ai.example.agent.rag.repository.DocumentVectorRepository;
import com.cloud.alibaba.ai.example.agent.rag.repository.FileUploadRepository;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author chenshimiao
 */

@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Autowired
    private ChunkInfoRepository chunkInfoRepository;

    @Autowired
    private DocumentVectorRepository documentVectorRepository;

    @Value("${minio.publicUrl}")
    private String minioPublicUrl;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Override
    public void upload(MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        String bucketName = "rag";

        // 检查存储桶是否存在，不存在则创建
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            System.out.println("存储桶 '" + bucketName + "' 已创建");
        }

        // 生成 fileMd5（使用文件名+时间戳的哈希）
        String fileMd5 = java.util.UUID.randomUUID().toString().replace("-", "");

        PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object("upload/" + fileMd5 + "/" + file.getOriginalFilename()).contentType(file.getContentType()).stream(file.getInputStream(), file.getSize(), -1).build();

        minioClient.putObject(args);

        List<Document> docs = new TikaDocumentReader(file.getResource()).get();
        TokenTextSplitter splitter = new TokenTextSplitter(500, 50, 10, 500, true);
        List<Document> splitDocs = splitter.apply(docs);

        // 为每个 Document 添加 fileMd5 metadata
        for (Document doc : splitDocs) {
            doc.getMetadata().put("fileMd5", fileMd5);
            doc.getMetadata().put("fileName", file.getOriginalFilename());
        }

        vectorStore.add(splitDocs);

        // 保存文件上传记录
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileMd5(fileMd5);
        fileUpload.setFileName(file.getOriginalFilename());
        fileUpload.setTotalSize(file.getSize());
        fileUpload.setStatus(1); // 已完成
        fileUpload.setUserId("default");
        fileUpload.setMergedAt(java.time.LocalDateTime.now());
        fileUploadRepository.save(fileUpload);

        System.out.println("文件上传成功: " + file.getOriginalFilename() + ", fileMd5: " + fileMd5);
    }

    @Transactional
    @Override
    public void uploadChunk(@RequestParam("fileMd5") String fileMd5, MultipartFile file, int chunkIndex, long totalSize, String chunkMd5, HttpServletRequest request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        try {

            boolean fileExists = fileUploadRepository.findByFileMd5(fileMd5).isPresent();

            if (!fileExists) {
                FileUpload fileUpload = new FileUpload();
                fileUpload.setFileMd5(fileMd5);
                fileUpload.setFileName(file.getOriginalFilename());
                fileUpload.setTotalSize(totalSize);
                // 0表示上传中
                fileUpload.setStatus(0);
                // 设置默认用户ID
                fileUpload.setUserId("default");

                try {
                    fileUploadRepository.save(fileUpload);
                } catch (Exception e) {
                    throw new RuntimeException("创建文件记录失败: " + e.getMessage(), e);
                }
            }

            // 先检查这片有没有上传过
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(fileMd5, chunkIndex))) {
                return;
            }

            // 否则就上传这一片
            String objectName = "upload/" + fileMd5 + "/chunk/" + chunkIndex;
            PutObjectArgs args = PutObjectArgs.builder().bucket("rag").object(objectName).contentType(file.getContentType()).stream(file.getInputStream(), file.getSize(), -1).build();
            minioClient.putObject(args);

            redisTemplate.opsForValue().setBit(fileMd5, chunkIndex, true);

            // 存储分片信息
            ChunkInfo chunkInfo = new ChunkInfo();
            chunkInfo.setFileMd5(fileMd5);
            chunkInfo.setChunkIndex(chunkIndex);
            chunkInfo.setChunkMd5(chunkMd5);
            chunkInfo.setStoragePath(objectName);

            chunkInfoRepository.save(chunkInfo);
        } catch (Exception exception) {
            System.out.println("执行失败" + exception.getMessage());
        }
    }

    @Override
    public String mergeFile(UploadController.MergeRequest mergeRequest) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 判断文件是否全部上传完成
        FileUpload fileUpload = fileUploadRepository.findByFileMd5(mergeRequest.fileMd5()).orElse(null);

        if (fileUpload == null) {
            throw new RuntimeException("没有寻找到文件");
        }

        // 检查文件是否已经合并完成
        if (fileUpload.getStatus() == 1) {
            throw new RuntimeException("文件已合并完成，请勿重复操作");
        }

        // 获取所有已上传的分片
        List<ChunkInfo> byFileMd5 = chunkInfoRepository.findByFileMd5(mergeRequest.fileMd5());

        if (byFileMd5.isEmpty()) {
            throw new RuntimeException("没有找到分片数据");
        }

        String targetObjectName = "upload/" + mergeRequest.fileMd5() + "/" + mergeRequest.fileName();
        long totalSize = fileUpload.getTotalSize();
        long minChunkSize = 5 * 1024 * 1024; // 5MB - MinIO composeObject 最小分片要求

        // 判断是否可以使用 composeObject（所有分片都必须 >= 5MB）
        // 对于单分片或所有分片都小于5MB的情况，使用 copyObject
        if (totalSize < minChunkSize || byFileMd5.size() == 1) {
            // 小文件或单分片：直接复制第一个分片
            ChunkInfo chunkInfo = byFileMd5.get(0);
            minioClient.copyObject(CopyObjectArgs.builder().bucket(bucketName).object(targetObjectName).source(CopySource.builder().bucket(bucketName).object(chunkInfo.getStoragePath()).build()).build());
        } else {
            // 大文件：使用 composeObject 合并
            List<ComposeSource> sources = byFileMd5.stream().sorted((a, b) -> a.getChunkIndex() - b.getChunkIndex()).map(path -> ComposeSource.builder().bucket(bucketName).object(path.getStoragePath()).build()).collect(Collectors.toList());

            minioClient.composeObject(ComposeObjectArgs.builder().bucket(bucketName).object(targetObjectName).sources(sources).build());
        }

        // 使用 MinIO 客户端生成预签名 URL（有效期7天）
        try {
            String presignedObjectUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucketName).object(targetObjectName).expiry(7 * 24 * 60 * 60) // 7天有效期
                    .build());

            TikaDocumentReader reader = new TikaDocumentReader(presignedObjectUrl);
            List<Document> documents = reader.get();

            // 使用 TokenTextSplitter 进行分块，确保每块不超过 500 tokens
            TokenTextSplitter splitter = new TokenTextSplitter(500, 50, 10, 500, true);
            List<Document> splitDocuments = splitter.apply(documents);

            // 为每个 Document 添加 fileMd5 metadata
            for (Document doc : splitDocuments) {
                doc.getMetadata().put("fileMd5", mergeRequest.fileMd5());
                doc.getMetadata().put("fileName", mergeRequest.fileName());
            }

            vectorStore.add(splitDocuments);

            // 更新状态为上传完成
            fileUpload.setStatus(1);
            fileUpload.setMergedAt(java.time.LocalDateTime.now());
            fileUploadRepository.save(fileUpload);

            return presignedObjectUrl;
        } catch (Exception e) {
            throw new RuntimeException("生成文件访问URL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FileUpload> listDocuments() {
        return fileUploadRepository.findByStatus(1);
    }

    @Transactional
    @Override
    public void deleteDocument(String fileMd5) throws Exception {
        // 1. 查询文件记录
        FileUpload fileUpload = fileUploadRepository.findByFileMd5(fileMd5)
                .orElseThrow(() -> new RuntimeException("文档不存在: " + fileMd5));

        // 2. 删除 MinIO 中的文件对象
        String targetObjectName = "upload/" + fileMd5 + "/" + fileUpload.getFileName();
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(targetObjectName)
                    .build());
        } catch (Exception e) {
            System.out.println("删除 MinIO 主文件失败（可能不存在）: " + e.getMessage());
        }

        // 3. 删除 MinIO 中的分片对象
        List<ChunkInfo> chunks = chunkInfoRepository.findByFileMd5(fileMd5);
        for (ChunkInfo chunk : chunks) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(chunk.getStoragePath())
                        .build());
            } catch (Exception e) {
                System.out.println("删除 MinIO 分片失败: " + chunk.getStoragePath() + ", " + e.getMessage());
            }
        }

        // 4. 删除 Redis 中的上传状态
        try {
            redisTemplate.delete(fileMd5);
        } catch (Exception e) {
            System.out.println("删除 Redis 状态失败: " + e.getMessage());
        }

        // 5. 删除向量存储中的向量数据（使用正确的 Filter 表达式删除）
        try {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            Filter.Expression expression = b.eq("fileMd5", fileMd5).build();
            vectorStore.delete(expression);
            System.out.println("向量数据删除成功: " + fileMd5);
        } catch (Exception e) {
            System.out.println("删除向量数据失败（可能不存在）: " + e.getMessage());
        }

        // 6. 删除数据库记录（按依赖顺序）
        // 6.1 删除 document_vectors 表记录
        documentVectorRepository.deleteByFileMd5(fileMd5);

        // 6.2 删除 chunk_info 表记录
        chunkInfoRepository.deleteByFileMd5(fileMd5);

        // 6.3 删除 file_upload 表记录
        fileUploadRepository.deleteByFileMd5(fileMd5);

        System.out.println("文档删除成功: " + fileMd5);
    }
}
