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

package com.iqkv.foundation.cms.page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.iqkv.foundation.cms.page.dto.PageDtoMapper;
import com.iqkv.foundation.cms.page.dto.PageDtos;
import com.iqkv.foundation.cms.shared.exception.PageNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PageServiceImpl implements PageService {

  private final PageMapper pageMapper;

  public PageServiceImpl(final PageMapper pageMapper) {
    this.pageMapper = pageMapper;
  }

  @Override
  @Transactional
  public PageDtos.PageResponse create(final PageDtos.CreatePageRequest request) {
    validateEnUsTranslation(request.translations());
    final Page page = PageDtoMapper.toEntity(request);
    page.setId(UUID.randomUUID());
    page.setCreatedAt(LocalDateTime.now());
    page.setUpdatedAt(LocalDateTime.now());

    pageMapper.insert(page);

    for (final PageTranslation translation : page.getTranslations()) {
      translation.setPageId(page.getId());
      pageMapper.insertTranslation(translation);
    }

    return PageDtoMapper.toResponse(page);
  }

  @Override
  @Transactional
  public PageDtos.PageResponse update(final UUID id, final PageDtos.UpdatePageRequest request) {
    final Page page = pageMapper.findById(id)
        .orElseThrow(() -> new PageNotFoundException(id));

    if (page.getStatus() == PageStatus.PUBLISHED) {
      throw new IllegalStateException("Cannot modify a published page");
    }

    validateEnUsTranslation(request.translations());

    page.setSlug(request.slug());
    page.setParentId(request.parentId());
    page.setTemplate(request.template());
    page.setStatus(request.status());
    page.setUpdatedAt(LocalDateTime.now());
    pageMapper.update(page);

    pageMapper.deleteTranslations(id);
    for (final PageDtos.PageTranslationRequest tr : request.translations()) {
      final PageTranslation translation = new PageTranslation();
      translation.setPageId(id);
      translation.setLocale(tr.locale());
      translation.setTitle(tr.title());
      translation.setContent(tr.content());
      translation.setSeoTitle(tr.seoTitle());
      translation.setSeoDescription(tr.seoDescription());
      translation.setSeoOpenGraphTitle(tr.seoOpenGraphTitle());
      translation.setSeoOpenGraphDescription(tr.seoOpenGraphDescription());
      translation.setSeoCanonicalUrl(tr.seoCanonicalUrl());
      pageMapper.insertTranslation(translation);
    }

    return getById(id);
  }

  @Override
  @Transactional
  public void delete(final UUID id) {
    final Page page = pageMapper.findById(id)
        .orElseThrow(() -> new PageNotFoundException(id));

    if (page.getStatus() == PageStatus.PUBLISHED) {
      throw new IllegalStateException("Cannot delete a published page");
    }

    pageMapper.deleteTranslations(id);
    pageMapper.delete(id);
  }

  private void validateEnUsTranslation(final List<? extends PageDtos.PageTranslationRequest> translations) {
    final boolean hasEnUs = translations.stream()
        .anyMatch(t -> "en-US".equalsIgnoreCase(t.locale()));
    if (!hasEnUs) {
      throw new IllegalArgumentException("English (en-US) translation is mandatory");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageDtos.PageResponse getById(final UUID id) {
    return pageMapper.findById(id)
        .map(PageDtoMapper::toResponse)
        .orElseThrow(() -> new PageNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public PageDtos.PageResponse getBySlug(final String slug, final Locale locale) {
    final Page page = pageMapper.findPublishedBySlug(slug)
        .orElseThrow(() -> new PageNotFoundException(slug));
    applyFallbackToPage(page, locale);
    return PageDtoMapper.toResponse(page);
  }

  @Override
  @Transactional(readOnly = true)
  public PageDtos.PageListResponse getAll(final int limit, final int offset) {
    final List<Page> pages = pageMapper.findAll(limit, offset);
    final long totalElements = pageMapper.countAll();

    final List<PageDtos.PageResponse> items = pages.stream()
        .map(PageDtoMapper::toResponse)
        .toList();

    return new PageDtos.PageListResponse(items, totalElements);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageDtos.PageResponse> getPublishedByLocale(final Locale locale) {
    final List<Page> pages = pageMapper.findPublished();
    final List<Page> processedPages = new ArrayList<>();
    for (Page page : pages) {
      processedPages.add(applyFallbackToPage(page, locale));
    }
    return processedPages.stream()
        .map(PageDtoMapper::toResponse)
        .toList();
  }

  private Page applyFallbackToPage(final Page page, final Locale targetLocale) {
    final List<PageTranslation> translations = page.getTranslations();

    // First, try exact match (e.g., "en-US" → "en-US")
    Optional<PageTranslation> targetTranslation = translations.stream()
        .filter(t -> targetLocale.toLanguageTag().equalsIgnoreCase(t.getLocale()))
        .findFirst();

    if (targetTranslation.isEmpty()) {
      // Then, try language-only match (e.g., "en" → "en-US")
      targetTranslation = translations.stream()
          .filter(t -> t.getLocale().toLowerCase().startsWith(targetLocale.getLanguage().toLowerCase() + "-"))
          .findFirst();
    }

    if (targetTranslation.isEmpty()) {
      // Finally, try language-only without region (e.g., "en" → "en")
      targetTranslation = translations.stream()
          .filter(t -> t.getLocale().equalsIgnoreCase(targetLocale.getLanguage()))
          .findFirst();
    }

    if (targetTranslation.isPresent()) {
      page.setTranslations(List.of(targetTranslation.get()));
      return page;
    }

    // Fallback to en-US
    Optional<PageTranslation> enUsTranslation = translations.stream()
        .filter(t -> "en-US".equalsIgnoreCase(t.getLocale()))
        .findFirst();

    if (enUsTranslation.isPresent()) {
      page.setTranslations(List.of(enUsTranslation.get()));
      return page;
    }

    throw new IllegalStateException("Page has no en-US translation");
  }
}
