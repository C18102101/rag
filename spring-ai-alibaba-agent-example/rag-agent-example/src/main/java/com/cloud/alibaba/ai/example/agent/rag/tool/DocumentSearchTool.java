package com.cloud.alibaba.ai.example.agent.rag.tool;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentSearchTool {

    private final VectorStore vectorStore;

    public DocumentSearchTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public record Request(String query) {
    }

    public record Response(String content) {
    }

    public Response search(Request request) {
        // 从向量存储检索相关文档
        List<Document> docs = vectorStore.similaritySearch(request.query());

        // 合并文档内容
        String combinedContent = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(""));

        return new Response(combinedContent);
    }
}
