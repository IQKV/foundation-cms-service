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

package com.iqkv.foundation.cmsservice.page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Page {

  private UUID id;
  private String slug;
  private UUID parentId;
  private String template;
  private PageStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<PageTranslation> translations = new ArrayList<>();

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

  public List<PageTranslation> getTranslations() {
    return translations;
  }

  public void setTranslations(final List<PageTranslation> translations) {
    this.translations = translations;
  }
}
