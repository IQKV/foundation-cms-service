/*
 * Copyright 2026 IQKV Foundation Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iqkv.foundation.cms.page.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.iqkv.foundation.cms.page.PageStatus;

/**
 * Flat MyBatis result class for the admin summary list query.
 * Maps the LEFT JOIN result (page + single en-US translation title) without the full
 * collection overhead of {@link com.iqkv.foundation.cms.page.Page}.
 */
public class PageSummaryRow {

  private UUID id;
  private String slug;
  private UUID parentId;
  private String template;
  private PageStatus status;
  private String title;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(final String slug) {
    this.slug = slug;
  }

  public UUID getParentId() {
    return parentId;
  }

  public void setParentId(final UUID parentId) {
    this.parentId = parentId;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(final String template) {
    this.template = template;
  }

  public PageStatus getStatus() {
    return status;
  }

  public void setStatus(final PageStatus status) {
    this.status = status;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(final LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
