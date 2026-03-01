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
 * 文件分块信息实体类
 *
 * @author zth9
 * @since 2026-03-01
 */
@Entity
@Table(name = "chunk_info")
public class ChunkInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "file_md5", nullable = false, length = 32)
	private String fileMd5;

	@Column(name = "chunk_index", nullable = false)
	private Integer chunkIndex;

	@Column(name = "chunk_md5", nullable = false, length = 32)
	private String chunkMd5;

	@Column(name = "storage_path", nullable = false, length = 255)
	private String storagePath;

	public ChunkInfo() {
	}

	public ChunkInfo(String fileMd5, Integer chunkIndex, String chunkMd5, String storagePath) {
		this.fileMd5 = fileMd5;
		this.chunkIndex = chunkIndex;
		this.chunkMd5 = chunkMd5;
		this.storagePath = storagePath;
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

	public Integer getChunkIndex() {
		return chunkIndex;
	}

	public void setChunkIndex(Integer chunkIndex) {
		this.chunkIndex = chunkIndex;
	}

	public String getChunkMd5() {
		return chunkMd5;
	}

	public void setChunkMd5(String chunkMd5) {
		this.chunkMd5 = chunkMd5;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}
}
