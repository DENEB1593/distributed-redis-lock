package io.dev.deneb.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "spring.redis")
public record RedisProperties(String host, String port) {
}
