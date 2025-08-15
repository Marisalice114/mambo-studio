package com.hachimi.mamboaiplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

//启用缓存
@EnableCaching
@SpringBootApplication(exclude = {
        dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration.class
})
@EnableAspectJAutoProxy(exposeProxy = true)
//可在业务逻辑中获取当前的代理对象
@MapperScan("com.hachimi.mamboaiplatform.mapper")
@EnableScheduling
public class MamboAiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MamboAiPlatformApplication.class, args);
    }

}
