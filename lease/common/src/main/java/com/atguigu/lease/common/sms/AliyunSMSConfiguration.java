package com.atguigu.lease.common.sms;


import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
@EnableConfigurationProperties(AliyunSMSProperties.class)
@ConditionalOnProperty(prefix = "aliyun.sms", name = {"endpoint", "access-key-id", "access-key-secret"})
public class AliyunSMSConfiguration {

    @Bean
    public Client createClient(AliyunSMSProperties properties) {
        Config config = new Config()
                .setAccessKeyId(properties.getAccessKeyId())
                .setAccessKeySecret(properties.getAccessKeySecret())
                .setEndpoint(properties.getEndpoint());
        try {
            return new Client(config);
        } catch (Exception e) {
            throw new RuntimeException("阿里云SMS客户端初始化失败", e);
        }
    }
}



