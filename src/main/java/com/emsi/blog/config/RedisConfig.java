package com.emsi.blog.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
// import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
// import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;

    @Value("${spring.redis.url:}")
    private String redisUrl;

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.username:}")
    private String redisUsername;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.ssl:false}")
    private boolean redisSsl;

    @Value("${spring.redis.cache.ttl:10}")
    private long cacheTtlMinutes;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisConnectionProperties props = parseRedisProperties();
        RedisStandaloneConfiguration config = buildRedisConfig(props);
        LettuceClientConfiguration clientConfig = buildClientConfig(props.isUseSsl());
        
        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory redisConnectionFactory) {
        ObjectMapper redisMapper = objectMapper.copy();
        
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.emsi.blog")
                .allowIfSubType("java.util")
                .allowIfSubType("java.lang")
                .build();
        
        redisMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(redisMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(cacheTtlMinutes))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }

    private RedisConnectionProperties parseRedisProperties() {
        String host = redisHost;
        int port = redisPort;
        String username = redisUsername;
        String password = redisPassword;
        boolean useSsl = redisSsl;

        if (redisUrl != null && !redisUrl.isBlank()) {
            try {
                URI uri = new URI(redisUrl);
                useSsl = "rediss".equalsIgnoreCase(uri.getScheme());
                
                if (uri.getHost() != null) {
                    host = uri.getHost();
                }
                if (uri.getPort() != -1) {
                    port = uri.getPort();
                }

                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isBlank()) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts.length == 2 ? parts[0] : "";
                    password = parts.length == 2 ? parts[1] : parts[0];
                }
            } catch (URISyntaxException e) {
                log.error("Invalid spring.redis.url: {} — falling back to individual properties", redisUrl, e);
            }
        }

        return new RedisConnectionProperties(host, port, username, password, useSsl);
    }

    private RedisStandaloneConfiguration buildRedisConfig(RedisConnectionProperties props) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(props.getHost(), props.getPort());
        
        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            config.setUsername(props.getUsername());
        }
        if (props.getPassword() != null && !props.getPassword().isBlank()) {
            config.setPassword(RedisPassword.of(props.getPassword()));
        }
        
        return config;
    }

    private LettuceClientConfiguration buildClientConfig(boolean useSsl) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
        
        if (useSsl) {
            builder.useSsl();
        }
        
        return builder.build();
    }

    private static class RedisConnectionProperties {
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final boolean useSsl;

        public RedisConnectionProperties(String host, int port, String username, String password, boolean useSsl) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.useSsl = useSsl;
        }

        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public boolean isUseSsl() { return useSsl; }
    }
}
