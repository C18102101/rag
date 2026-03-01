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
 * 组织标签实体类
 *
 * @author zth9
 * @since 2026-03-01
 */
@Entity
@Table(name = "organization_tags")
public class OrganizationTag {

	@Id
	@Column(length = 255)
	private String tagId;

	@Column(nullable = false, length = 100)
	private String name;

	private String description;

	@Column(length = 255)
	private String parentTag;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@Column(insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	public OrganizationTag() {
	}

	public OrganizationTag(String tagId, String name, User createdBy) {
		this.tagId = tagId;
		this.name = name;
		this.createdBy = createdBy;
	}

	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getParentTag() {
		return parentTag;
	}

	public void setParentTag(String parentTag) {
		this.parentTag = parentTag;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
