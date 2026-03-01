package com.cloud.alibaba.ai.example.agent.rag.Service;

import com.cloud.alibaba.ai.example.agent.rag.controller.UploadController;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author chenshimiao
 */
public interface UploadService {

    void upload(MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     * 分片上传
     *
     * @param file
     * @param chunk
     * @param totalSize
     * @param chunkMd5
     * @param request
     */
    void uploadChunk(String fileMd5, MultipartFile file, int chunk, long totalSize, String chunkMd5, HttpServletRequest request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     * merge文件
     * @param mergeRequest
     * @return 文件访问地址
     */
    String mergeFile(UploadController.MergeRequest mergeRequest) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     * 获取所有已上传完成的文档列表
     * @return 文档列表
     */
    java.util.List<com.cloud.alibaba.ai.example.agent.rag.entity.FileUpload> listDocuments();

    /**
     * 删除指定文档及其所有关联数据
     * @param fileMd5 文件MD5
     * @throws Exception 删除过程中的异常
     */
    void deleteDocument(String fileMd5) throws Exception;
}
