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

package com.iqkv.foundation.servicename.plan;

import java.util.Collections;
import java.util.Map;

/**
 * Local copy of the plan feature set as returned by the billing service internal plans endpoint.
 *
 * <p>Intentionally a plain record — no shared library dependency on billing service.
 * Only the fields this service cares about are mapped; unknown JSON fields are ignored.
 *
 * <h3>Design — middle path</h3>
 * <ul>
 *   <li><b>Typed quota fields</b> ({@code maxUsers}, {@code maxProjects}) — kept as named
 *       {@code int} fields for compile-time safety. Enforce these at write time.</li>
 *   <li><b>Open feature map</b> ({@link #features}) — extensible
 *       {@code Map<String, PlanFeature>} keyed by feature code. O(1) lookup,
 *       no duplicate codes, insertion order preserved. Adding a new feature requires
 *       only a YAML change in the billing service — no recompilation here.</li>
 * </ul>
 *
 * <p>Add typed fields only for quotas this service actually enforces at write time.
 * Everything else belongs in the {@code features} map.
 *
 * <p>{@code maxUsers} and {@code maxProjects} use {@code 0} to mean "unlimited".
 *
 * <p>Usage example — quota enforcement and feature check at write time:
 * <pre>
 *   final PlanFeatures f = planCatalogCache.forPlan(request.getHeader("X-Plan-Code"));
 *   if (f.maxProjects() > 0 &amp;&amp; current &gt;= f.maxProjects()) {
 *       throw new PlanQuotaExceededException(...);
 *   }
 *   if (f.has("custom_domain")) { ... }
 * </pre>
 */
public record PlanFeatures(
    int maxUsers,
    int maxProjects,
    Map<String, PlanFeature> features
) {

  /**
   * Safe fallback when the plan code is unknown or the cache is empty.
   */
  public static final PlanFeatures NONE = new PlanFeatures(1, 1, Collections.emptyMap());

  public PlanFeatures {
    features = features != null ? Collections.unmodifiableMap(features) : Collections.emptyMap();
  }

  /**
   * Returns {@code true} if the feature map contains an entry for the given code
   * whose value is {@code "true"} (case-insensitive). O(1) lookup.
   *
   * <p>Use this for display-only boolean features. For quota enforcement use the
   * typed fields {@link #maxUsers()} and {@link #maxProjects()} directly.
   *
   * @param code the feature code (e.g. {@code "priority_support"})
   */
  public boolean has(final String code) {
    if (code == null || code.isBlank()) {
      return false;
    }
    final PlanFeature feature = features.get(code);
    return feature != null && "true".equalsIgnoreCase(feature.value());
  }
}
