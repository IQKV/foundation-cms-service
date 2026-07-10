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

package com.iqkv.foundation.cmsservice.page.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.iqkv.foundation.cmsservice.page.Page;
import com.iqkv.foundation.cmsservice.page.PageStatus;
import com.iqkv.foundation.cmsservice.page.PageTranslation;
import org.junit.jupiter.api.Test;

class PageDtoMapperTest {

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  private static PageTranslation translation(final String locale, final String title) {
    final var t = new PageTranslation();
    t.setLocale(locale);
    t.setTitle(title);
    t.setContent("content-" + locale);
    t.setSeoTitle("seo-title-" + locale);
    t.setSeoDescription("seo-desc-" + locale);
    t.setSeoOpenGraphTitle("og-title-" + locale);
    t.setSeoOpenGraphDescription("og-desc-" + locale);
    t.setSeoCanonicalUrl("https://example.com/" + locale);
    return t;
  }

  private static Page page(final PageStatus status, final PageTranslation... translations) {
    final var p = new Page();
    p.setId(UUID.randomUUID());
    p.setSlug("test-slug");
    p.setParentId(null);
    p.setTemplate("default");
    p.setStatus(status);
    p.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
    p.setUpdatedAt(LocalDateTime.of(2026, 6, 1, 12, 0));
    p.setTranslations(List.of(translations));
    return p;
  }

  private static PageSummaryRow summaryRow() {
    final var row = new PageSummaryRow();
    row.setId(UUID.randomUUID());
    row.setSlug("summary-slug");
    row.setParentId(UUID.randomUUID());
    row.setTemplate("wide");
    row.setStatus(PageStatus.PUBLISHED);
    row.setTitle("Summary Title");
    row.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
    row.setUpdatedAt(LocalDateTime.of(2026, 6, 1, 0, 0));
    return row;
  }

  // -----------------------------------------------------------------------
  // toResponse
  // -----------------------------------------------------------------------

  @Test
  void toResponse_mapsPageFields() {
    final var p = page(PageStatus.DRAFT, translation("en-US", "Home"));

    final var resp = PageDtoMapper.toResponse(p);

    assertThat(resp.id()).isEqualTo(p.getId());
    assertThat(resp.slug()).isEqualTo("test-slug");
    assertThat(resp.parentId()).isNull();
    assertThat(resp.template()).isEqualTo("default");
    assertThat(resp.status()).isEqualTo(PageStatus.DRAFT);
    assertThat(resp.createdAt()).isEqualTo(p.getCreatedAt());
    assertThat(resp.updatedAt()).isEqualTo(p.getUpdatedAt());
  }

  @Test
  void toResponse_mapsTranslationFields() {
    final var p = page(PageStatus.PUBLISHED, translation("en-US", "Home"), translation("fr-FR", "Accueil"));

    final var resp = PageDtoMapper.toResponse(p);

    assertThat(resp.translations()).hasSize(2);

    final var enUs = resp.translations().get(0);
    assertThat(enUs.locale()).isEqualTo("en-US");
    assertThat(enUs.title()).isEqualTo("Home");
    assertThat(enUs.content()).isEqualTo("content-en-US");
    assertThat(enUs.seoTitle()).isEqualTo("seo-title-en-US");
    assertThat(enUs.seoDescription()).isEqualTo("seo-desc-en-US");
    assertThat(enUs.seoOpenGraphTitle()).isEqualTo("og-title-en-US");
    assertThat(enUs.seoOpenGraphDescription()).isEqualTo("og-desc-en-US");
    assertThat(enUs.seoCanonicalUrl()).isEqualTo("https://example.com/en-US");

    final var frFr = resp.translations().get(1);
    assertThat(frFr.locale()).isEqualTo("fr-FR");
    assertThat(frFr.title()).isEqualTo("Accueil");
  }

  @Test
  void toResponse_emptyTranslationsList() {
    final var p = page(PageStatus.DRAFT);

    final var resp = PageDtoMapper.toResponse(p);

    assertThat(resp.translations()).isEmpty();
  }

  @Test
  void toResponse_withParentId() {
    final var parentId = UUID.randomUUID();
    final var p = page(PageStatus.DRAFT, translation("en-US", "Child"));
    p.setParentId(parentId);

    final var resp = PageDtoMapper.toResponse(p);

    assertThat(resp.parentId()).isEqualTo(parentId);
  }

  // -----------------------------------------------------------------------
  // toSummaryResponse
  // -----------------------------------------------------------------------

  @Test
  void toSummaryResponse_mapsAllFields() {
    final var row = summaryRow();

    final var resp = PageDtoMapper.toSummaryResponse(row);

    assertThat(resp.id()).isEqualTo(row.getId());
    assertThat(resp.slug()).isEqualTo("summary-slug");
    assertThat(resp.parentId()).isEqualTo(row.getParentId());
    assertThat(resp.template()).isEqualTo("wide");
    assertThat(resp.status()).isEqualTo(PageStatus.PUBLISHED);
    assertThat(resp.title()).isEqualTo("Summary Title");
    assertThat(resp.createdAt()).isEqualTo(row.getCreatedAt());
    assertThat(resp.updatedAt()).isEqualTo(row.getUpdatedAt());
  }

  @Test
  void toSummaryResponse_nullParentId_preserved() {
    final var row = summaryRow();
    row.setParentId(null);

    final var resp = PageDtoMapper.toSummaryResponse(row);

    assertThat(resp.parentId()).isNull();
  }

  @Test
  void toSummaryResponse_nullTitle_preserved() {
    final var row = summaryRow();
    row.setTitle(null);

    final var resp = PageDtoMapper.toSummaryResponse(row);

    assertThat(resp.title()).isNull();
  }

  // -----------------------------------------------------------------------
  // toEntity
  // -----------------------------------------------------------------------

  @Test
  void toEntity_mapsPageFields() {
    final var parentId = UUID.randomUUID();
    final var request = new PageDtos.CreatePageRequest(
        "about", parentId, "wide", PageStatus.DRAFT,
        List.of(new PageDtos.PageTranslationRequest("en-US", "About", "Body", null, null, null, null, null)));

    final var entity = PageDtoMapper.toEntity(request);

    assertThat(entity.getSlug()).isEqualTo("about");
    assertThat(entity.getParentId()).isEqualTo(parentId);
    assertThat(entity.getTemplate()).isEqualTo("wide");
    assertThat(entity.getStatus()).isEqualTo(PageStatus.DRAFT);
  }

  @Test
  void toEntity_mapsTranslationFields() {
    final var request = new PageDtos.CreatePageRequest(
        "home", null, "default", PageStatus.DRAFT,
        List.of(new PageDtos.PageTranslationRequest(
            "en-US", "Home", "Welcome", "SEO T", "SEO D", "OG T", "OG D", "https://canon.com")));

    final var entity = PageDtoMapper.toEntity(request);
    final var t = entity.getTranslations().get(0);

    assertThat(t.getLocale()).isEqualTo("en-US");
    assertThat(t.getTitle()).isEqualTo("Home");
    assertThat(t.getContent()).isEqualTo("Welcome");
    assertThat(t.getSeoTitle()).isEqualTo("SEO T");
    assertThat(t.getSeoDescription()).isEqualTo("SEO D");
    assertThat(t.getSeoOpenGraphTitle()).isEqualTo("OG T");
    assertThat(t.getSeoOpenGraphDescription()).isEqualTo("OG D");
    assertThat(t.getSeoCanonicalUrl()).isEqualTo("https://canon.com");
  }

  @Test
  void toEntity_multipleTranslations() {
    final var request = new PageDtos.CreatePageRequest(
        "home", null, "default", PageStatus.DRAFT,
        List.of(
            new PageDtos.PageTranslationRequest("en-US", "Home", null, null, null, null, null, null),
            new PageDtos.PageTranslationRequest("de-DE", "Startseite", null, null, null, null, null, null)));

    final var entity = PageDtoMapper.toEntity(request);

    assertThat(entity.getTranslations()).hasSize(2);
    assertThat(entity.getTranslations()).extracting("locale").containsExactly("en-US", "de-DE");
  }

  @Test
  void toEntity_doesNotAssignId_orTimestamps() {
    final var request = new PageDtos.CreatePageRequest(
        "slug", null, "default", PageStatus.DRAFT,
        List.of(new PageDtos.PageTranslationRequest("en-US", "T", null, null, null, null, null, null)));

    final var entity = PageDtoMapper.toEntity(request);

    // ID and timestamps are assigned by PageServiceImpl, not the mapper
    assertThat(entity.getId()).isNull();
    assertThat(entity.getCreatedAt()).isNull();
    assertThat(entity.getUpdatedAt()).isNull();
  }

  @Test
  void toEntity_translationPageIdIsNull_beforeServiceAssignsIt() {
    final var request = new PageDtos.CreatePageRequest(
        "slug", null, "default", PageStatus.DRAFT,
        List.of(new PageDtos.PageTranslationRequest("en-US", "T", null, null, null, null, null, null)));

    final var entity = PageDtoMapper.toEntity(request);

    assertThat(entity.getTranslations().get(0).getPageId()).isNull();
  }
}
