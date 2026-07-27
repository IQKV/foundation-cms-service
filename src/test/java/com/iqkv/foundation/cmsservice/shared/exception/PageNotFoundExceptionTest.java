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

package com.iqkv.foundation.cmsservice.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PageNotFoundExceptionTest {

  @Test
  void constructWithUuid_messageContainsId() {
    final var id = UUID.randomUUID();

    final var ex = new PageNotFoundException(id);

    assertThat(ex).isInstanceOf(RuntimeException.class);
    assertThat(ex.getMessage()).contains(id.toString());
  }

  @Test
  void constructWithSlug_messageContainsSlug() {
    final var ex = new PageNotFoundException("about-us");

    assertThat(ex).isInstanceOf(RuntimeException.class);
    assertThat(ex.getMessage()).contains("about-us");
  }

  @Test
  void constructWithUuid_noCause() {
    final var ex = new PageNotFoundException(UUID.randomUUID());

    assertThat(ex.getCause()).isNull();
  }

  @Test
  void constructWithSlug_noCause() {
    final var ex = new PageNotFoundException("slug");

    assertThat(ex.getCause()).isNull();
  }

  @Test
  void twoDistinctUuids_produceDistinctMessages() {
    final var id1 = UUID.randomUUID();
    final var id2 = UUID.randomUUID();

    assertThat(new PageNotFoundException(id1).getMessage())
        .isNotEqualTo(new PageNotFoundException(id2).getMessage());
  }
}
