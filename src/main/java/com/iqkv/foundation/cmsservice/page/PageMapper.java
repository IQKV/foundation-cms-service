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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.iqkv.foundation.cmsservice.page.dto.PageHierarchyRow;
import com.iqkv.foundation.cmsservice.page.dto.PageSummaryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PageMapper {

  void insert(Page page);

  void insertTranslation(PageTranslation translation);

  void update(Page page);

  void deleteTranslations(@Param("pageId") UUID pageId);

  void delete(@Param("id") UUID id);

  Optional<Page> findById(@Param("id") UUID id);

  Optional<Page> findBySlug(@Param("slug") String slug);

  List<Page> findAll(@Param("limit") int limit, @Param("offset") int offset);

  List<PageSummaryRow> findAllSummary(@Param("limit") int limit, @Param("offset") int offset);

  List<PageHierarchyRow> findAllForHierarchy();

  long countAll();

  List<Page> findPublished();

  Optional<Page> findPublishedBySlug(@Param("slug") String slug);

}
