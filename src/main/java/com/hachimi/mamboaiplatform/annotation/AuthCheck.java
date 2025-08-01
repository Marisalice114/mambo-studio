package com.hachimi.mamboaiplatform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须的权限级别
     *   - 公开访问（默认）
     * user - 需要登录
     * vip - 需要VIP
     * admin - 需要管理员
     */
    String mustRole() default "";

}

