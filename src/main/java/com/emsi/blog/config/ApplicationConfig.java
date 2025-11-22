package com.emsi.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.emsi.blog.user.UserRepository;

import lombok.RequiredArgsConstructor;

// new caching / redis imports
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.CacheManager;
import org.springframework.beans.factory.annotation.Value;

// new imports for URI parsing
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// new imports for polymorphic typing
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@EnableCaching
public class ApplicationConfig {

    private final UserRepository userRepository;

    // full URI support
    @Value("${spring.redis.url:}")
    private String redisUrl;

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    // optional username (for ACL-enabled Redis)
    @Value("${spring.redis.username:}")
    private String redisUsername;

    // optional password
    @Value("${spring.redis.password:}")
    private String redisPassword;

    // optional SSL flag (false by default)
    @Value("${spring.redis.ssl:false}")
    private boolean redisSsl;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        
        return new BCryptPasswordEncoder();
    }

    // Redis connection factory using Lettuce (honors spring.redis.url or individual props)
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        String host = redisHost;
        int port = redisPort;
        String username = redisUsername;
        String password = redisPassword;
        boolean useSsl = redisSsl;

        if (redisUrl != null && !redisUrl.isBlank()) {
            try {
                URI uri = new URI(redisUrl);
                String scheme = uri.getScheme(); // redis or rediss
                if ("rediss".equalsIgnoreCase(scheme)) {
                    useSsl = true;
                } else if ("redis".equalsIgnoreCase(scheme)) {
                    useSsl = false;
                }

                if (uri.getHost() != null) {
                    host = uri.getHost();
                }
                if (uri.getPort() != -1) {
                    port = uri.getPort();
                }

                String userInfo = uri.getUserInfo(); // user:password or password
                if (userInfo != null && !userInfo.isBlank()) {
                    String[] parts = userInfo.split(":", 2);
                    if (parts.length == 2) {
                        username = parts[0];
                        password = parts[1];
                    } else {
                        // only password provided
                        password = parts[0];
                    }
                }
            } catch (URISyntaxException e) {
                // invalid URI — fall back to individual properties (log for debugging)
                System.err.println("Invalid spring.redis.url: " + redisUrl + " — falling back to individual properties. Error: " + e.getMessage());
            }
        }

        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration(host, port);
        if (username != null && !username.isBlank()) {
            cfg.setUsername(username);
        }
        if (password != null && !password.isBlank()) {
            cfg.setPassword(RedisPassword.of(password));
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                // .useSsl(useSsl)
                .build();

        return new LettuceConnectionFactory(cfg, clientConfig);
    }

    // Redis-backed CacheManager with JSON value serializer and String keys
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory redisConnectionFactory) {
        // build ObjectMapper that supports constructor-based deserialization and polymorphic typing
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule()) // enables constructor parameter name support
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(MapperFeature.INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES);

        // enable polymorphic type handling so GenericJackson2JsonRedisSerializer stores type info
        // allow application package types AND java.util/java.lang so collections and primitives deserialize correctly
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.emsi.blog")
                .allowIfSubType("java.util")
                .allowIfSubType("java.lang")
                .build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // default TTL (change as needed)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }

}
