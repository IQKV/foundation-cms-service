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

package com.iqkv.foundation.cmsservice.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InvalidPlatformModeExceptionTest {

  @Test
  void constructWithMessage_storesMessage() {
    final var ex = new InvalidPlatformModeException("rollout mode not configured");

    assertThat(ex).isInstanceOf(RuntimeException.class);
    assertThat(ex.getMessage()).isEqualTo("rollout mode not configured");
    assertThat(ex.getCause()).isNull();
  }

  @Test
  void constructWithMessageAndCause_storesBoth() {
    final var cause = new IllegalArgumentException("bad value");

    final var ex = new InvalidPlatformModeException("invalid mode: XYZ", cause);

    assertThat(ex.getMessage()).isEqualTo("invalid mode: XYZ");
    assertThat(ex.getCause()).isSameAs(cause);
  }

  @Test
  void isRuntimeException_doesNotRequireDeclaration() {
    // Verify it is unchecked — assignable to RuntimeException
    final RuntimeException ex = new InvalidPlatformModeException("msg");

    assertThat(ex).isNotNull();
  }
}
