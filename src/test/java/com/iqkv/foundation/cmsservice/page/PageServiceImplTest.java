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

package com.iqkv.foundation.cmsservice.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.iqkv.foundation.cmsservice.page.dto.PageDtos;
import com.iqkv.foundation.cmsservice.page.dto.PageHierarchyRow;
import com.iqkv.foundation.cmsservice.page.dto.PageSummaryRow;
import com.iqkv.foundation.cmsservice.shared.exception.PageNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PageServiceImplTest {

  @Mock
  private PageMapper pageMapper;

  @InjectMocks
  private PageServiceImpl service;

  private UUID pageId;

  @BeforeEach
  void setUp() {
    pageId = UUID.randomUUID();
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  private static PageTranslation translation(final String locale) {
    final var t = new PageTranslation();
    t.setLocale(locale);
    t.setTitle("Title " + locale);
    t.setContent("Content " + locale);
    return t;
  }

  private static Page draftPage(final UUID id) {
    final var p = new Page();
    p.setId(id);
    p.setSlug("slug");
    p.setTemplate("default");
    p.setStatus(PageStatus.DRAFT);
    p.setCreatedAt(LocalDateTime.now());
    p.setUpdatedAt(LocalDateTime.now());
    p.setTranslations(List.of(translation("en-US")));
    return p;
  }

  private static Page publishedPage(final UUID id) {
    final var p = draftPage(id);
    p.setStatus(PageStatus.PUBLISHED);
    return p;
  }

  private static PageDtos.CreatePageRequest createRequest(final String... locales) {
    final var translations = java.util.Arrays.stream(locales)
        .map(l -> new PageDtos.PageTranslationRequest(l, "Title " + l, null, null, null, null, null, null))
        .toList();
    return new PageDtos.CreatePageRequest("slug", null, "default", PageStatus.DRAFT, translations);
  }

  private static PageDtos.UpdatePageRequest updateRequest(final String... locales) {
    final var translations = java.util.Arrays.stream(locales)
        .map(l -> new PageDtos.PageTranslationRequest(l, "Title " + l, null, null, null, null, null, null))
        .toList();
    return new PageDtos.UpdatePageRequest("new-slug", null, "wide", PageStatus.DRAFT, translations);
  }

  // -----------------------------------------------------------------------
  // create
  // -----------------------------------------------------------------------

  @Test
  void create_returnsResponse() {
    final var request = createRequest("en-US");

    final var result = service.create(request);

    assertThat(result).isNotNull();
    assertThat(result.slug()).isEqualTo("slug");
    verify(pageMapper).insert(any(Page.class));
    verify(pageMapper).insertTranslation(any(PageTranslation.class));
  }

  @Test
  void create_assignsNewId() {
    final var request = createRequest("en-US");

    service.create(request);

    verify(pageMapper).insert(any(Page.class));
  }

  @Test
  void create_insertsOneTranslationPerEntry() {
    final var request = createRequest("en-US", "fr-FR", "de-DE");

    service.create(request);

    // one insertTranslation call per locale
    verify(pageMapper, org.mockito.Mockito.times(3)).insertTranslation(any(PageTranslation.class));
  }

  @Test
  void create_throwsWhenNoEnUsTranslation() {
    final var request = createRequest("fr-FR");

    assertThatThrownBy(() -> service.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("en-US");

    verify(pageMapper, never()).insert(any());
  }

  @Test
  void create_enUsCheckIsCaseInsensitive() {
    // "EN-US" must be accepted as a valid en-US translation
    final var request = createRequest("EN-US");

    final var result = service.create(request);

    assertThat(result).isNotNull();
  }

  // -----------------------------------------------------------------------
  // update
  // -----------------------------------------------------------------------

  @Test
  void update_returnsUpdatedResponse() {
    final var page = draftPage(pageId);
    when(pageMapper.findById(pageId)).thenReturn(Optional.of(page));
    // getById is called at the end of update
    final var updatedPage = draftPage(pageId);
    updatedPage.setSlug("new-slug");
    updatedPage.setTranslations(List.of(translation("en-US")));
    when(pageMapper.findById(pageId)).thenReturn(Optional.of(page)).thenReturn(Optional.of(updatedPage));

    final var result = service.update(pageId, updateRequest("en-US"));

    assertThat(result).isNotNull();
    verify(pageMapper).update(any(Page.class));
    verify(pageMapper).deleteTranslations(pageId);
    verify(pageMapper).insertTranslation(any(PageTranslation.class));
  }

  @Test
  void update_throwsWhenPageNotFound() {
    when(pageMapper.findById(pageId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(pageId, updateRequest("en-US")))
        .isInstanceOf(PageNotFoundException.class);

    verify(pageMapper, never()).update(any());
  }

  @Test
  void update_throwsWhenPageIsPublished() {
    when(pageMapper.findById(pageId)).thenReturn(Optional.of(publishedPage(pageId)));

    assertThatThrownBy(() -> service.update(pageId, updateRequest("en-US")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("published");

    verify(pageMapper, never()).update(any());
  }

  @Test
  void update_throwsWhenNoEnUsTranslation() {
    when(pageMapper.findById(pageId)).thenReturn(Optional.of(draftPage(pageId)));

    assertThatThrownBy(() -> service.update(pageId, updateRequest("de-DE")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("en-US");

    verify(pageMapper, never()).update(any());
  }

  // -----------------------------------------------------------------------
  // delete
  // -----------------------------------------------------------------------

  @Test
  void delete_removesTranslationsAndPage() {
    when(pageMapper.findById(pageId)).thenReturn(Optional.of(draftPage(pageId)));

    service.delete(pageId);

    verify(pageMapper).deleteTranslations(pageId);
    verify(pageMapper).delete(pageId);
  }

  @Test
  void delete_throwsWhenPageNotFound() {
    when(pageMapper.findById(pageId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(pageId))
        .isInstanceOf(PageNotFoundException.class);

    verify(pageMapper, never()).delete(any());
  }

  @Test
  void delete_throwsWhenPageIsPublished() {
    when(pageMapper.findById(pageId)).thenReturn(Optional.of(publishedPage(pageId)));

    assertThatThrownBy(() -> service.delete(pageId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("published");

    verify(pageMapper, never()).delete(any());
  }

  // -----------------------------------------------------------------------
  // getById
  // -----------------------------------------------------------------------

  @Test
  void getById_returnsResponse() {
    when(pageMapper.findById(pageId)).thenReturn(Optional.of(draftPage(pageId)));

    final var result = service.getById(pageId);

    assertThat(result.id()).isEqualTo(pageId);
    assertThat(result.slug()).isEqualTo("slug");
  }

  @Test
  void getById_throwsWhenNotFound() {
    when(pageMapper.findById(pageId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(pageId))
        .isInstanceOf(PageNotFoundException.class)
        .hasMessageContaining(pageId.toString());
  }

  // -----------------------------------------------------------------------
  // getBySlug
  // -----------------------------------------------------------------------

  @Test
  void getBySlug_returnsExactLocaleMatch() {
    final var page = draftPage(pageId);
    page.setTranslations(List.of(translation("en-US"), translation("fr-FR")));
    when(pageMapper.findPublishedBySlug("slug")).thenReturn(Optional.of(page));

    final var result = service.getBySlug("slug", Locale.forLanguageTag("fr-FR"));

    assertThat(result.translations()).hasSize(1);
    assertThat(result.translations().get(0).locale()).isEqualTo("fr-FR");
  }

  @Test
  void getBySlug_fallsBackToLanguageMatch() {
    final var page = draftPage(pageId);
    // Only "fr-FR" available, request "fr" (language only)
    page.setTranslations(List.of(translation("en-US"), translation("fr-FR")));
    when(pageMapper.findPublishedBySlug("slug")).thenReturn(Optional.of(page));

    final var result = service.getBySlug("slug", Locale.forLanguageTag("fr"));

    assertThat(result.translations()).hasSize(1);
    assertThat(result.translations().get(0).locale()).isEqualTo("fr-FR");
  }

  @Test
  void getBySlug_fallsBackToEnUs() {
    final var page = draftPage(pageId);
    page.setTranslations(List.of(translation("en-US")));
    when(pageMapper.findPublishedBySlug("slug")).thenReturn(Optional.of(page));

    final var result = service.getBySlug("slug", Locale.forLanguageTag("ja-JP"));

    assertThat(result.translations()).hasSize(1);
    assertThat(result.translations().get(0).locale()).isEqualTo("en-US");
  }

  @Test
  void getBySlug_throwsWhenSlugNotFound() {
    when(pageMapper.findPublishedBySlug("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getBySlug("missing", Locale.forLanguageTag("en-US")))
        .isInstanceOf(PageNotFoundException.class)
        .hasMessageContaining("missing");
  }

  @Test
  void getBySlug_throwsWhenNoEnUsAndNoMatchingTranslation() {
    final var page = draftPage(pageId);
    page.setTranslations(List.of(translation("de-DE")));
    when(pageMapper.findPublishedBySlug("slug")).thenReturn(Optional.of(page));

    assertThatThrownBy(() -> service.getBySlug("slug", Locale.forLanguageTag("fr-FR")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("en-US");
  }

  // -----------------------------------------------------------------------
  // getAll
  // -----------------------------------------------------------------------

  @Test
  void getAll_returnsItemsAndTotal() {
    when(pageMapper.findAll(10, 0)).thenReturn(List.of(draftPage(pageId)));
    when(pageMapper.countAll()).thenReturn(1L);

    final var result = service.getAll(10, 0);

    assertThat(result.items()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1L);
  }

  @Test
  void getAll_returnsEmptyWhenNoPagesExist() {
    when(pageMapper.findAll(10, 0)).thenReturn(List.of());
    when(pageMapper.countAll()).thenReturn(0L);

    final var result = service.getAll(10, 0);

    assertThat(result.items()).isEmpty();
    assertThat(result.totalElements()).isZero();
  }

  // -----------------------------------------------------------------------
  // getAllSummary
  // -----------------------------------------------------------------------

  @Test
  void getAllSummary_returnsItemsAndTotal() {
    final var row = new PageSummaryRow();
    row.setId(pageId);
    row.setSlug("slug");
    row.setStatus(PageStatus.DRAFT);
    row.setTitle("My Page");
    row.setCreatedAt(LocalDateTime.now());
    row.setUpdatedAt(LocalDateTime.now());

    when(pageMapper.findAllSummary(5, 0)).thenReturn(List.of(row));
    when(pageMapper.countAll()).thenReturn(1L);

    final var result = service.getAllSummary(5, 0);

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().get(0).id()).isEqualTo(pageId);
    assertThat(result.items().get(0).title()).isEqualTo("My Page");
    assertThat(result.totalElements()).isEqualTo(1L);
  }

  // -----------------------------------------------------------------------
  // getHierarchyItems
  // -----------------------------------------------------------------------

  @Test
  void getHierarchyItems_mapsRows() {
    final var row = new PageHierarchyRow();
    row.setId(pageId);
    row.setSlug("about");
    row.setParentId(null);
    row.setTitle("About Us");

    when(pageMapper.findAllForHierarchy()).thenReturn(List.of(row));

    final var result = service.getHierarchyItems();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(pageId);
    assertThat(result.get(0).slug()).isEqualTo("about");
    assertThat(result.get(0).title()).isEqualTo("About Us");
    assertThat(result.get(0).parentId()).isNull();
  }

  @Test
  void getHierarchyItems_returnsEmptyList() {
    when(pageMapper.findAllForHierarchy()).thenReturn(List.of());

    assertThat(service.getHierarchyItems()).isEmpty();
  }

  // -----------------------------------------------------------------------
  // getPublishedByLocale
  // -----------------------------------------------------------------------

  @Test
  void getPublishedByLocale_returnsMatchingTranslation() {
    final var page = draftPage(pageId);
    page.setTranslations(List.of(translation("en-US"), translation("fr-FR")));
    when(pageMapper.findPublished()).thenReturn(List.of(page));

    final var result = service.getPublishedByLocale(Locale.forLanguageTag("fr-FR"));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).translations().get(0).locale()).isEqualTo("fr-FR");
  }

  @Test
  void getPublishedByLocale_fallsBackToEnUsForUnknownLocale() {
    final var page = draftPage(pageId);
    page.setTranslations(List.of(translation("en-US")));
    when(pageMapper.findPublished()).thenReturn(List.of(page));

    final var result = service.getPublishedByLocale(Locale.forLanguageTag("zh-CN"));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).translations().get(0).locale()).isEqualTo("en-US");
  }

  @Test
  void getPublishedByLocale_returnsEmptyWhenNoPublishedPages() {
    when(pageMapper.findPublished()).thenReturn(List.of());

    assertThat(service.getPublishedByLocale(Locale.forLanguageTag("en-US"))).isEmpty();
  }
}
