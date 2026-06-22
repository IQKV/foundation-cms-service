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

import java.util.List;
import java.util.Locale;

import com.iqkv.foundation.cms.page.dto.PageDtos;
import com.iqkv.foundation.cms.tenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cms/pages")
@Tag(name = "Public Pages", description = "Endpoints for retrieving published CMS pages (tenant-scoped)")
public class PageRestResource {

  private final PageService pageService;

  public PageRestResource(final PageService pageService) {
    this.pageService = pageService;
  }

  @GetMapping
  @Operation(summary = "Get published pages", description = "Retrieves a list of all published CMS pages for a specific locale (from Accept-Language header or ?lang=xxx or ?locale=xxx), with en-US fallback.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Published pages retrieved successfully")
  })
  public ResponseEntity<List<PageDtos.PageResponse>> getPublishedByLocale(
      @Parameter(description = "8-char tenant key") @RequestHeader("X-Tenant-ID") final String tenantKey,
      @Parameter(description = "Locale override (e.g., en-US, fr-FR)") @RequestParam(required = false) final String locale,
      final Locale resolvedLocale) {
    try {
      TenantContext.setCurrentTenant(tenantKey);
      final Locale finalLocale = locale != null ? Locale.forLanguageTag(locale) : resolvedLocale;
      return ResponseEntity.ok(pageService.getPublishedByLocale(finalLocale));
    } finally {
      TenantContext.clear();
    }
  }

  @GetMapping("/{slug}")
  @Operation(summary = "Get page by slug", description = "Retrieves a single published CMS page by its slug, with en-US fallback for translations (uses Accept-Language header or ?lang=xxx or ?locale=xxx).")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Page retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Page not found")
  })
  public ResponseEntity<PageDtos.PageResponse> getBySlug(
      @Parameter(description = "8-char tenant key") @RequestHeader("X-Tenant-ID") final String tenantKey,
      @PathVariable final String slug,
      @Parameter(description = "Locale override (e.g., en-US, fr-FR)") @RequestParam(required = false) final String locale,
      final Locale resolvedLocale) {
    try {
      TenantContext.setCurrentTenant(tenantKey);
      final Locale finalLocale = locale != null ? Locale.forLanguageTag(locale) : resolvedLocale;
      return ResponseEntity.ok(pageService.getBySlug(slug, finalLocale));
    } finally {
      TenantContext.clear();
    }
  }
}
