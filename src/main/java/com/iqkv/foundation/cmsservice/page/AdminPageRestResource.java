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

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import com.iqkv.foundation.cmsservice.page.dto.PageDtos;
import com.iqkv.foundation.tenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/api/v1/cms/admin/{tenantKey}/pages")
@Tag(name = "Admin Page Management", description = "Platform admin endpoints for managing CMS pages (tenant-scoped)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
public class AdminPageRestResource {

  private final PageService pageService;

  public AdminPageRestResource(final PageService pageService) {
    this.pageService = pageService;
  }

  @GetMapping("/hierarchy")
  @Operation(
      summary = "List pages for hierarchy picker",
      description = "Returns a flat list of all CMS pages for the tenant (id, slug, parentId, en-US title). "
          + "Intended for use in the parent-page selector on the admin create/edit forms.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Hierarchy items returned"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<List<PageDtos.PageHierarchyItem>> getHierarchy(
      @Parameter(description = "8-char tenant key") @PathVariable final String tenantKey) {
    try {
      TenantContext.setCurrentTenant(tenantKey);
      return ResponseEntity.ok(pageService.getHierarchyItems());
    } finally {
      TenantContext.clear();
    }
  }

  @PostMapping
  @Operation(summary = "Create page", description = "Creates a new CMS page with multi-lingual translations for the specified tenant.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Page created successfully"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<PageDtos.PageResponse> create(
      @Parameter(description = "8-char tenant key") @PathVariable final String tenantKey,
      @Valid @RequestBody final PageDtos.CreatePageRequest request) {
    try {
      TenantContext.setCurrentTenant(tenantKey);
      return ResponseEntity.status(HttpStatus.CREATED).body(pageService.create(request));
    } finally {
      TenantContext.clear();
    }
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update page", description = "Updates an existing CMS page and its translations for the specified tenant.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Page updated successfully"),
      @ApiResponse(responseCode = "404", description = "Page not found")
  })
  public ResponseEntity<PageDtos.PageResponse> update(
      @Parameter(description = "8-char tenant key") @PathVariable final String tenantKey,
      @PathVariable final UUID id,
      @Valid @RequestBody final PageDtos.UpdatePageRequest request) {
    try {
      TenantContext.setCurrentTenant(tenantKey);
      return ResponseEntity.ok(pageService.update(id, request));
    } finally {
      TenantContext.clear();
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete page", description = "Deletes a CMS page for the specified tenant.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Page deleted successfully")
  })
  public ResponseEntity<Void> delete(
      @Parameter(description = "8-char tenant key") @PathVariable final String tenantKey,
      @PathVariable final UUID id) {
    try {
      TenantContext.setCurrentTenant(tenantKey);
      pageService.delete(id);
      return ResponseEntity.noContent().build();
    } finally {
      TenantContext.clear();
    }
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get page by ID", description = "Retrieves detailed information about a specific page for the specified tenant.")
  public ResponseEntity<PageDtos.PageResponse> getById(
      @Parameter(description = "8-char tenant key") @PathVariable final String tenantKey,
      @PathVariable final UUID id) {
    try {
      TenantContext.setCurrentTenant(tenantKey);
      return ResponseEntity.ok(pageService.getById(id));
    } finally {
      TenantContext.clear();
    }
  }

  @GetMapping
  @Operation(summary = "List all pages (summary)", description = "Retrieves a paginated summary list of all CMS pages for the specified tenant, including the en-US fallback title.")
  public ResponseEntity<PageDtos.PageSummaryListResponse> getAllSummary(
      @Parameter(description = "8-char tenant key") @PathVariable final String tenantKey,
      @RequestParam(defaultValue = "20") final int limit,
      @RequestParam(defaultValue = "0") final int offset) {
    try {
      TenantContext.setCurrentTenant(tenantKey);
      return ResponseEntity.ok(pageService.getAllSummary(limit, offset));
    } finally {
      TenantContext.clear();
    }
  }
}
