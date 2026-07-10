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

import com.iqkv.foundation.cmsservice.page.PageStatus;
import org.junit.jupiter.api.Test;

class PageDtosTest {

  // -----------------------------------------------------------------------
  // PageTranslationRequest
  // -----------------------------------------------------------------------

  @Test
  void translationRequest_storesAllFields() {
    var req = new PageDtos.PageTranslationRequest(
        "en-US", "Title", "Body",
        "SEO Title", "SEO Desc",
        "OG Title", "OG Desc", "https://example.com/canonical");

    assertThat(req.locale()).isEqualTo("en-US");
    assertThat(req.title()).isEqualTo("Title");
    assertThat(req.content()).isEqualTo("Body");
    assertThat(req.seoTitle()).isEqualTo("SEO Title");
    assertThat(req.seoDescription()).isEqualTo("SEO Desc");
    assertThat(req.seoOpenGraphTitle()).isEqualTo("OG Title");
    assertThat(req.seoOpenGraphDescription()).isEqualTo("OG Desc");
    assertThat(req.seoCanonicalUrl()).isEqualTo("https://example.com/canonical");
  }

  @Test
  void translationRequest_equalityOnSameValues() {
    var a = new PageDtos.PageTranslationRequest("en-US", "T", "C", null, null, null, null, null);
    var b = new PageDtos.PageTranslationRequest("en-US", "T", "C", null, null, null, null, null);

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  void translationRequest_acceptsNullOptionalFields() {
    var req = new PageDtos.PageTranslationRequest("fr-FR", "Titre", null, null, null, null, null, null);

    assertThat(req.locale()).isEqualTo("fr-FR");
    assertThat(req.content()).isNull();
    assertThat(req.seoTitle()).isNull();
  }

  // -----------------------------------------------------------------------
  // CreatePageRequest
  // -----------------------------------------------------------------------

  @Test
  void createRequest_storesAllFields() {
    var id = UUID.randomUUID();
    var translations = List.of(
        new PageDtos.PageTranslationRequest("en-US", "Home", null, null, null, null, null, null));
    var req = new PageDtos.CreatePageRequest("home", id, "default", PageStatus.DRAFT, translations);

    assertThat(req.slug()).isEqualTo("home");
    assertThat(req.parentId()).isEqualTo(id);
    assertThat(req.template()).isEqualTo("default");
    assertThat(req.status()).isEqualTo(PageStatus.DRAFT);
    assertThat(req.translations()).hasSize(1);
  }

  @Test
  void createRequest_nullParentId_allowed() {
    var req = new PageDtos.CreatePageRequest("root", null, "default", PageStatus.DRAFT, List.of(
        new PageDtos.PageTranslationRequest("en-US", "Root", null, null, null, null, null, null)));

    assertThat(req.parentId()).isNull();
  }

  // -----------------------------------------------------------------------
  // UpdatePageRequest
  // -----------------------------------------------------------------------

  @Test
  void updateRequest_storesAllFields() {
    var req = new PageDtos.UpdatePageRequest("about", null, "wide", PageStatus.PUBLISHED, List.of(
        new PageDtos.PageTranslationRequest("en-US", "About", null, null, null, null, null, null)));

    assertThat(req.slug()).isEqualTo("about");
    assertThat(req.status()).isEqualTo(PageStatus.PUBLISHED);
    assertThat(req.template()).isEqualTo("wide");
  }

  // -----------------------------------------------------------------------
  // PageResponse
  // -----------------------------------------------------------------------

  @Test
  void pageResponse_storesAllFields() {
    var id = UUID.randomUUID();
    var now = LocalDateTime.now();
    var translations = List.of(
        new PageDtos.PageTranslationResponse("en-US", "T", "C", null, null, null, null, null));
    var resp = new PageDtos.PageResponse(id, "slug", null, "default", PageStatus.DRAFT, now, now, translations);

    assertThat(resp.id()).isEqualTo(id);
    assertThat(resp.slug()).isEqualTo("slug");
    assertThat(resp.status()).isEqualTo(PageStatus.DRAFT);
    assertThat(resp.translations()).hasSize(1);
    assertThat(resp.createdAt()).isEqualTo(now);
  }

  @Test
  void pageResponse_equality() {
    var id = UUID.randomUUID();
    var now = LocalDateTime.of(2026, 1, 1, 0, 0);
    var t = List.of(new PageDtos.PageTranslationResponse("en-US", "T", null, null, null, null, null, null));
    var a = new PageDtos.PageResponse(id, "s", null, "d", PageStatus.DRAFT, now, now, t);
    var b = new PageDtos.PageResponse(id, "s", null, "d", PageStatus.DRAFT, now, now, t);

    assertThat(a).isEqualTo(b);
  }

  // -----------------------------------------------------------------------
  // PageListResponse
  // -----------------------------------------------------------------------

  @Test
  void pageListResponse_storesTotalElements() {
    var list = new PageDtos.PageListResponse(List.of(), 42L);

    assertThat(list.items()).isEmpty();
    assertThat(list.totalElements()).isEqualTo(42L);
  }

  // -----------------------------------------------------------------------
  // PageSummaryResponse
  // -----------------------------------------------------------------------

  @Test
  void pageSummaryResponse_storesAllFields() {
    var id = UUID.randomUUID();
    var now = LocalDateTime.now();
    var resp = new PageDtos.PageSummaryResponse(id, "slug", null, "default", PageStatus.PUBLISHED, "My Page", now, now);

    assertThat(resp.id()).isEqualTo(id);
    assertThat(resp.title()).isEqualTo("My Page");
    assertThat(resp.status()).isEqualTo(PageStatus.PUBLISHED);
  }

  // -----------------------------------------------------------------------
  // PageSummaryListResponse
  // -----------------------------------------------------------------------

  @Test
  void pageSummaryListResponse_storesTotalElements() {
    var list = new PageDtos.PageSummaryListResponse(List.of(), 7L);

    assertThat(list.totalElements()).isEqualTo(7L);
    assertThat(list.items()).isEmpty();
  }

  // -----------------------------------------------------------------------
  // PageHierarchyItem
  // -----------------------------------------------------------------------

  @Test
  void pageHierarchyItem_storesAllFields() {
    var id = UUID.randomUUID();
    var parentId = UUID.randomUUID();
    var item = new PageDtos.PageHierarchyItem(id, "about", parentId, "About Us");

    assertThat(item.id()).isEqualTo(id);
    assertThat(item.slug()).isEqualTo("about");
    assertThat(item.parentId()).isEqualTo(parentId);
    assertThat(item.title()).isEqualTo("About Us");
  }

  @Test
  void pageHierarchyItem_nullParentId_allowed() {
    var item = new PageDtos.PageHierarchyItem(UUID.randomUUID(), "root", null, "Root");

    assertThat(item.parentId()).isNull();
  }
}
