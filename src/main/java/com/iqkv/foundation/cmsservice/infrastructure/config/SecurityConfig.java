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

package com.iqkv.foundation.cmsservice.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import com.iqkv.foundation.cmsservice.infrastructure.security.CorrelationIdFilter;
import com.iqkv.foundation.cmsservice.infrastructure.security.JwtClaimNames;
import com.iqkv.foundation.cmsservice.tenancy.TenantExtractionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final AuthConfigurationProperties authProps;
  private final ResourceLoader resourceLoader;
  private final TenantExtractionFilter tenantExtractionFilter;
  private final CorrelationIdFilter correlationIdFilter;

  public SecurityConfig(final AuthConfigurationProperties authProps,
                        final ResourceLoader resourceLoader,
                        @Lazy final TenantExtractionFilter tenantExtractionFilter,
                        final CorrelationIdFilter correlationIdFilter) {
    this.authProps = authProps;
    this.resourceLoader = resourceLoader;
    this.tenantExtractionFilter = tenantExtractionFilter;
    this.correlationIdFilter = correlationIdFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/api-docs/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/swagger-ui.html").permitAll()
            .requestMatchers("/api/v1/cms/pages/**").permitAll()
            .requestMatchers("/api/v1/cms/admin/**").hasAuthority("PLATFORM_ADMIN")
            .requestMatchers("/api/v1/cms/tenant/**").hasAnyAuthority("TENANT_OWNER", "ADMIN")
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .decoder(jwtDecoder())
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
        )
        .addFilterBefore(correlationIdFilter, BearerTokenAuthenticationFilter.class)
        .addFilterAfter(tenantExtractionFilter, CorrelationIdFilter.class);

    return http.build();
  }

  /**
   * Builds the JWT decoder based on the active verification strategy.
   *
   * <ul>
   *   <li>When {@code iqkv.auth.jwt.jwks-uri} is set: delegates to the IAM JWKS endpoint.
   *       Nimbus caches the key set in memory and re-fetches only on an unknown {@code kid},
   *       so key rotation in IAM is transparent to this service.</li>
   *   <li>When {@code iqkv.auth.jwt.public-key-path} is set: parses the RSA public key from
   *       the PEM file once at startup. No network dependency — suitable for local dev and tests.</li>
   * </ul>
   *
   * <p>Exactly one of the two properties must be configured; startup fails with an
   * {@link IllegalStateException} if both or neither are present (enforced by
   * {@link AuthConfigurationProperties#validate()}).
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    final AuthConfigurationProperties.Jwt jwt = authProps.jwt();

    if (jwt.jwksUri() != null && !jwt.jwksUri().isBlank()) {
      // Deployed (K8s): fetch public keys from the IAM JWKS endpoint.
      // Nimbus handles caching and re-fetch on unknown kid automatically.
      return NimbusJwtDecoder.withJwkSetUri(jwt.jwksUri()).build();
    }

    // Local dev / tests: parse the RSA public key from a PEM file.
    // Accepts classpath: resources (test) or file: paths (custom local setups).
    try {
      final String pem;
      try (InputStream is = resourceLoader.getResource(jwt.publicKeyPath()).getInputStream()) {
        pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      }
      final String stripped = pem
          .replace("-----BEGIN PUBLIC KEY-----", "")
          .replace("-----END PUBLIC KEY-----", "")
          .replaceAll("\\s", "");
      final byte[] keyBytes = Base64.getDecoder().decode(stripped);
      final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      final RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
      return NimbusJwtDecoder.withPublicKey(publicKey).build();
    } catch (final IOException | java.security.GeneralSecurityException e) {
      throw new IllegalStateException("Failed to load RSA public key for JWT decoding from: " + jwt.publicKeyPath(), e);
    }
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    final var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      final List<String> authorities = jwt.getClaimAsStringList(JwtClaimNames.AUTHORITIES);
      if (authorities == null) {
        return List.of();
      }
      return authorities.stream()
          .map(SimpleGrantedAuthority::new)
          .map(a -> (GrantedAuthority) a)
          .toList();
    });
    return converter;
  }
}
