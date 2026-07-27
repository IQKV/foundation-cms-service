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

import java.util.List;

import com.iqkv.foundation.cmsservice.page.Page;
import com.iqkv.foundation.cmsservice.page.PageTranslation;
import com.iqkv.foundation.cmsservice.page.dto.PageSummaryRow;

public final class PageDtoMapper {

  private PageDtoMapper() {
  }

  public static PageDtos.PageResponse toResponse(final Page page) {
    final List<PageDtos.PageTranslationResponse> translationResponses = page.getTranslations()
        .stream()
        .map(t -> new PageDtos.PageTranslationResponse(
            t.getLocale(),
            t.getTitle(),
            t.getContent(),
            t.getSeoTitle(),
            t.getSeoDescription(),
            t.getSeoOpenGraphTitle(),
            t.getSeoOpenGraphDescription(),
            t.getSeoCanonicalUrl()
        ))
        .toList();

    return new PageDtos.PageResponse(
        page.getId(),
        page.getSlug(),
        page.getParentId(),
        page.getTemplate(),
        page.getStatus(),
        page.getCreatedAt(),
        page.getUpdatedAt(),
        translationResponses
    );
  }

  public static PageDtos.PageSummaryResponse toSummaryResponse(final PageSummaryRow row) {
    return new PageDtos.PageSummaryResponse(
        row.getId(),
        row.getSlug(),
        row.getParentId(),
        row.getTemplate(),
        row.getStatus(),
        row.getTitle(),
        row.getCreatedAt(),
        row.getUpdatedAt()
    );
  }

  public static Page toEntity(final PageDtos.CreatePageRequest request) {
    final Page page = new Page();
    page.setSlug(request.slug());
    page.setParentId(request.parentId());
    page.setTemplate(request.template());
    page.setStatus(request.status());
    page.setTranslations(request.translations().stream()
        .map(t -> {
          final PageTranslation translation = new PageTranslation();
          translation.setLocale(t.locale());
          translation.setTitle(t.title());
          translation.setContent(t.content());
          translation.setSeoTitle(t.seoTitle());
          translation.setSeoDescription(t.seoDescription());
          translation.setSeoOpenGraphTitle(t.seoOpenGraphTitle());
          translation.setSeoOpenGraphDescription(t.seoOpenGraphDescription());
          translation.setSeoCanonicalUrl(t.seoCanonicalUrl());
          return translation;
        })
        .toList());
    return page;
  }
}
