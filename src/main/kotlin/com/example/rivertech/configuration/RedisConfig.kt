package com.example.rivertech.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericToStringSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
 class RedisConfig {

    @Bean
     fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Long> {
        return RedisTemplate<String, Long>().apply {
            setConnectionFactory(redisConnectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericToStringSerializer(Long::class.java)
        }
    }
}
