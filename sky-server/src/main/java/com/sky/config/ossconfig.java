package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/22
 * @time14:43 用于创建aliUtils对象
 **/
@Configuration
@Slf4j
public class ossconfig {

    @Bean//项目启动时自动调用该方法创建对象 ,把yml里的数据赋值给对象
    @ConditionalOnMissingBean//有这个对象是就不要新建
    public AliOssUtil aliOssUtil(AliOssProperties properties) {
        log.info("开始创建AliOssUtil对象{}", properties);
        return new AliOssUtil(
                properties.getEndpoint(), properties.getAccessKeyId(),
                properties.getAccessKeySecret(), properties.getBucketName());
    }
}
