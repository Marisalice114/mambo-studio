package com.hachimi.mamboaiplatform.ratelimit.annotation;

import com.hachimi.mamboaiplatform.ratelimit.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 普通用户每个时间窗口允许的请求数
     */
    int rate() default 10;

    /**
     * VIP用户每个时间窗口允许的请求数
     * 如果不设置，则与普通用户相同
     */
    int vipRate() default -1;

    /**
     * 时间窗口（秒）
     */
    int rateInterval() default 1;

    /**
     * 限流类型
     */
    RateLimitType limitType() default RateLimitType.USER;

    /**
     * 普通用户限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * VIP用户限流提示信息
     */
    String vipMessage() default "VIP用户请求过于频繁，请稍后再试";

    /**
     * 是否启用VIP差异化限流
     */
    boolean enableVipDifferentiation() default false;
}
