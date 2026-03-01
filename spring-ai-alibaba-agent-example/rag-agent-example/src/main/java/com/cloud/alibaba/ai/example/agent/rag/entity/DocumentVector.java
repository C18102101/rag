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

/**
 * 文档向量存储实体类
 *
 * @author zth9
 * @since 2026-03-01
 */
@Entity
@Table(name = "document_vectors")
public class DocumentVector {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "vector_id")
	private Long vectorId;

	@Column(name = "file_md5", nullable = false, length = 32)
	private String fileMd5;

	@Column(name = "chunk_id", nullable = false)
	private Integer chunkId;

	@Column(name = "text_content", columnDefinition = "TEXT")
	private String textContent;

	@Column(name = "model_version", length = 32)
	private String modelVersion;

	@Column(name = "user_id", nullable = false, length = 64)
	private String userId;

	@Column(name = "org_tag", length = 50)
	private String orgTag;

	@Column(name = "is_public", nullable = false)
	private Boolean isPublic = false;

	public DocumentVector() {
	}

	public DocumentVector(String fileMd5, Integer chunkId, String textContent, String userId) {
		this.fileMd5 = fileMd5;
		this.chunkId = chunkId;
		this.textContent = textContent;
		this.userId = userId;
	}

	public Long getVectorId() {
		return vectorId;
	}

	public void setVectorId(Long vectorId) {
		this.vectorId = vectorId;
	}

	public String getFileMd5() {
		return fileMd5;
	}

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}

	public Integer getChunkId() {
		return chunkId;
	}

	public void setChunkId(Integer chunkId) {
		this.chunkId = chunkId;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
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
}
