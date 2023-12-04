package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/28
 * @time13:37
 **/
@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory factory) {
        log.info("RedisConfig初始化");

        RedisTemplate redisTemplate = new RedisTemplate();

        //设置连接工厂
        redisTemplate.setConnectionFactory(factory);

        //设置key的序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());




        return redisTemplate;

    }

}
