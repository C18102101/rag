/*
 * Copyright 2026-2027 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloud.alibaba.ai.example.agent.rag.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 文件上传记录实体类
 *
 * @author zth9
 * @since 2026-03-01
 */
@Entity
@Table(name = "file_upload", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"file_md5", "user_id"})
})
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_md5", nullable = false, length = 32)
    private String fileMd5;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "total_size", nullable = false)
    private Long totalSize;

    @Column(nullable = false)
    private int status = 0;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "org_tag", length = 50)
    private String orgTag;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "merged_at")
    private LocalDateTime mergedAt;

    public FileUpload() {
    }

    public FileUpload(String fileMd5, String fileName, Long totalSize, String userId) {
        this.fileMd5 = fileMd5;
        this.fileName = fileName;
        this.totalSize = totalSize;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgTag() {
        return orgTag;
    }

    public void setOrgTag(String orgTag) {
        this.orgTag = orgTag;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getMergedAt() {
        return mergedAt;
    }

    public void setMergedAt(LocalDateTime mergedAt) {
        this.mergedAt = mergedAt;
    }
}
