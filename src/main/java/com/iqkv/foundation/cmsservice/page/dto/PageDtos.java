/*
 * Copyright 2026 iQKV Foundation Team.
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

package com.iqkv.foundation.cmsservice.page.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.iqkv.foundation.cmsservice.page.PageStatus;

public final class PageDtos {

  private PageDtos() {
  }

  public record PageTranslationRequest(
      String locale,
      String title,
      String content,
      String seoTitle,
      String seoDescription,
      String seoOpenGraphTitle,
      String seoOpenGraphDescription,
      String seoCanonicalUrl) {
  }

  public record CreatePageRequest(
      String slug,
      UUID parentId,
      String template,
      PageStatus status,
      List<PageTranslationRequest> translations) {
  }

  public record UpdatePageRequest(
      String slug,
      UUID parentId,
      String template,
      PageStatus status,
      List<PageTranslationRequest> translations) {
  }

  public record PageTranslationResponse(
      String locale,
      String title,
      String content,
      String seoTitle,
      String seoDescription,
      String seoOpenGraphTitle,
      String seoOpenGraphDescription,
      String seoCanonicalUrl) {
  }

  public record PageResponse(
      UUID id,
      String slug,
      UUID parentId,
      String template,
      PageStatus status,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      List<PageTranslationResponse> translations) {
  }

  public record PageListResponse(
      List<PageResponse> items,
      long totalElements) {
  }

  /**
   * Lightweight summary used by the admin list view.
   * Includes the en-US fallback title so the grid can display a human-readable name
   * without embedding all translation content.
   */
  public record PageSummaryResponse(
      UUID id,
      String slug,
      UUID parentId,
      String template,
      PageStatus status,
      String title,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
  }

  public record PageSummaryListResponse(
      List<PageSummaryResponse> items,
      long totalElements) {
  }

  /**
   * Lightweight item used to build a parent-page picker in the admin UI.
   * Returns all pages for the tenant with their en-US fallback title.
   */
  public record PageHierarchyItem(
      UUID id,
      String slug,
      UUID parentId,
      String title) {
  }
}
