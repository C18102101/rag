package com.cloud.alibaba.ai.example.agent.rag.controller;

import com.cloud.alibaba.ai.example.agent.rag.Service.UploadService;
import com.cloud.alibaba.ai.example.agent.rag.entity.FileUpload;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Controller
@RequestMapping("/api/rag")
public class UploadController {

    @GetMapping("/upload")
    public String uploadPage() {
        return "index";
    }

    @Autowired
    private UploadService uploadService;

    @PostMapping("/upload/chunk")
    @ResponseBody
    public ResponseEntity<String> uploadChunk(String fileMd5, @RequestParam("file") MultipartFile file, int chunk, long totalSize, String chunkMd5, HttpServletRequest request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 开始分片上传
        uploadService.uploadChunk(fileMd5, file, chunk, totalSize, chunkMd5, request);
        return ResponseEntity.ok("分片上传成功");
    }

    @PostMapping("/merge")
    @ResponseBody
    public ResponseEntity<String> mergeFile(@RequestBody MergeRequest mergeRequest) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String fileUrl = uploadService.mergeFile(mergeRequest);
        return ResponseEntity.ok(fileUrl);
    }

    public record MergeRequest(String fileMd5, String fileName) {
    }

    @PostMapping("/upload/file")
    @ResponseBody
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要上传的文件");
        }

        try {
            uploadService.upload(file);
            return ResponseEntity.ok("文件上传成功: " + file.getOriginalFilename());
        } catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            return ResponseEntity.internalServerError().body("上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有已上传完成的文档列表
     */
    @GetMapping("/documents")
    @ResponseBody
    public ResponseEntity<List<FileUpload>> listDocuments() {
        List<FileUpload> documents = uploadService.listDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * 删除指定文档及其所有关联数据
     */
    @DeleteMapping("/documents/{fileMd5}")
    @ResponseBody
    public ResponseEntity<String> deleteDocument(@PathVariable String fileMd5) {
        try {
            uploadService.deleteDocument(fileMd5);
            return ResponseEntity.ok("文档删除成功");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("删除失败: " + e.getMessage());
        }
    }
}
