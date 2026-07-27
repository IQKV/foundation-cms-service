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

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.iqkv.foundation.cmsservice.page.dto.PageDtos;

public interface PageService {

  PageDtos.PageResponse create(PageDtos.CreatePageRequest request);

  PageDtos.PageResponse update(UUID id, PageDtos.UpdatePageRequest request);

  void delete(UUID id);

  PageDtos.PageResponse getById(UUID id);

  PageDtos.PageResponse getBySlug(String slug, Locale locale);

  PageDtos.PageListResponse getAll(int limit, int offset);

  PageDtos.PageSummaryListResponse getAllSummary(int limit, int offset);

  List<PageDtos.PageHierarchyItem> getHierarchyItems();

  List<PageDtos.PageResponse> getPublishedByLocale(Locale locale);

}
