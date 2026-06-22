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

import java.util.UUID;

public class PageTranslation {

  private UUID pageId;
  private String locale;
  private String title;
  private String content;
  private String seoTitle;
  private String seoDescription;
  private String seoOpenGraphTitle;
  private String seoOpenGraphDescription;
  private String seoCanonicalUrl;

  public UUID getPageId() {
    return pageId;
  }

  public void setPageId(final UUID pageId) {
    this.pageId = pageId;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(final String locale) {
    this.locale = locale;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(final String content) {
    this.content = content;
  }

  public String getSeoTitle() {
    return seoTitle;
  }

  public void setSeoTitle(final String seoTitle) {
    this.seoTitle = seoTitle;
  }

  public String getSeoDescription() {
    return seoDescription;
  }

  public void setSeoDescription(final String seoDescription) {
    this.seoDescription = seoDescription;
  }

  public String getSeoOpenGraphTitle() {
    return seoOpenGraphTitle;
  }

  public void setSeoOpenGraphTitle(final String seoOpenGraphTitle) {
    this.seoOpenGraphTitle = seoOpenGraphTitle;
  }

  public String getSeoOpenGraphDescription() {
    return seoOpenGraphDescription;
  }

  public void setSeoOpenGraphDescription(final String seoOpenGraphDescription) {
    this.seoOpenGraphDescription = seoOpenGraphDescription;
  }

  public String getSeoCanonicalUrl() {
    return seoCanonicalUrl;
  }

  public void setSeoCanonicalUrl(final String seoCanonicalUrl) {
    this.seoCanonicalUrl = seoCanonicalUrl;
  }
}
