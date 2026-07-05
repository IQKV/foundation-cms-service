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

package com.iqkv.foundation.cmsservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the CMS Service.
 * Declares the tenant provisioning queue and binds it to the shared platform events exchange.
 */
@Configuration
@ConditionalOnProperty(name = "iqkv.messaging.rabbitmq.enabled", havingValue = "true")
public class RabbitMQConfig {

  // -------------------------------------------------------------------------
  // Shared exchange (all services publish here)
  // -------------------------------------------------------------------------
  public static final String EVENTS_EXCHANGE = "iqkv.events";
  public static final String DLX_EXCHANGE = "iqkv.dlx";

  // -------------------------------------------------------------------------
  // CMS queue names
  // -------------------------------------------------------------------------
  public static final String TENANT_PROVISIONING_QUEUE = "iqkv.cms.tenant.provisioning";
  public static final String CMS_DLQ = "iqkv.cms.tenant.provisioning.dlq";

  // -------------------------------------------------------------------------
  // Routing keys
  // -------------------------------------------------------------------------
  public static final String ROUTING_TENANT_CREATED = "tenant.created";

  private static final long TTL_24H_MS = 86_400_000L;

  // -------------------------------------------------------------------------
  // Beans
  // -------------------------------------------------------------------------

  @Bean
  public TopicExchange eventsExchange() {
    return new TopicExchange(EVENTS_EXCHANGE, true, false);
  }

  @Bean
  public TopicExchange deadLetterExchange() {
    return new TopicExchange(DLX_EXCHANGE, true, false);
  }

  @Bean
  public Queue tenantProvisioningQueue() {
    return QueueBuilder.durable(TENANT_PROVISIONING_QUEUE)
        .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
        .withArgument("x-message-ttl", TTL_24H_MS)
        .build();
  }

  @Bean
  public Queue cmsDeadLetterQueue() {
    return new Queue(CMS_DLQ, true);
  }

  @Bean
  public Binding tenantProvisioningBinding(final Queue tenantProvisioningQueue,
                                           final TopicExchange eventsExchange) {
    return BindingBuilder.bind(tenantProvisioningQueue).to(eventsExchange)
        .with(ROUTING_TENANT_CREATED);
  }

  @Bean
  public Binding cmsDlqBinding(final Queue cmsDeadLetterQueue,
                                final TopicExchange deadLetterExchange) {
    return BindingBuilder.bind(cmsDeadLetterQueue).to(deadLetterExchange).with(CMS_DLQ);
  }
}
