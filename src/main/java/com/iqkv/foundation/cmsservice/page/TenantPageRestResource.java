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

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import com.iqkv.foundation.cmsservice.page.dto.PageDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tenant-scoped CMS page management API.
 *
 * <p>Requires {@code TENANT_OWNER} or {@code ADMIN} authority.
 * The tenant context is resolved automatically from the JWT {@code tenant_id} claim
 * by {@link com.iqkv.foundation.cmsservice.tenancy.TenantExtractionFilter} — no tenant key
 * path variable is needed. All operations are confined to the authenticated tenant's schema.
 */
@RestController
@RequestMapping("/api/v1/cms/tenant/pages")
@Tag(name = "Tenant Page Management", description = "Tenant-scoped endpoints for managing CMS pages (TENANT_OWNER or ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyAuthority('TENANT_OWNER', 'ADMIN')")
public class TenantPageRestResource {

  private final PageService pageService;

  public TenantPageRestResource(final PageService pageService) {
    this.pageService = pageService;
  }

  @GetMapping("/hierarchy")
  @Operation(
      summary = "List pages for hierarchy picker",
      description = "Returns a flat list of all CMS pages for the authenticated tenant (id, slug, parentId, en-US title). "
          + "Intended for use in the parent-page selector on create/edit forms.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Hierarchy items returned"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<List<PageDtos.PageHierarchyItem>> getHierarchy() {
    return ResponseEntity.ok(pageService.getHierarchyItems());
  }

  @GetMapping
  @Operation(
      summary = "List all pages (summary)",
      description = "Retrieves a paginated summary list of all CMS pages for the authenticated tenant, "
          + "including the en-US fallback title.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Page list returned"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<PageDtos.PageSummaryListResponse> getAll(
      @RequestParam(defaultValue = "20") final int limit,
      @RequestParam(defaultValue = "0") final int offset) {
    return ResponseEntity.ok(pageService.getAllSummary(limit, offset));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get page by ID",
      description = "Retrieves full details of a specific CMS page (all translations) for the authenticated tenant.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Page returned"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Page not found")
  })
  public ResponseEntity<PageDtos.PageResponse> getById(@PathVariable final UUID id) {
    return ResponseEntity.ok(pageService.getById(id));
  }

  @PostMapping
  @Operation(
      summary = "Create page",
      description = "Creates a new CMS page with multi-lingual translations for the authenticated tenant.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Page created"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<PageDtos.PageResponse> create(
      @Valid @RequestBody final PageDtos.CreatePageRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(pageService.create(request));
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update page",
      description = "Updates an existing CMS page and its translations for the authenticated tenant.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Page updated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Page not found")
  })
  public ResponseEntity<PageDtos.PageResponse> update(
      @PathVariable final UUID id,
      @Valid @RequestBody final PageDtos.UpdatePageRequest request) {
    return ResponseEntity.ok(pageService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete page",
      description = "Deletes a CMS page for the authenticated tenant. Published pages cannot be deleted.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Page deleted"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Page not found")
  })
  public ResponseEntity<Void> delete(@PathVariable final UUID id) {
    pageService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
