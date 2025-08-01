package com.hachimi.mamboaiplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
//可在业务逻辑中获取当前的代理对象
@MapperScan("com.hachimi.mamboaiplatform.mapper")
public class MamboAiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MamboAiPlatformApplication.class, args);
    }

}
