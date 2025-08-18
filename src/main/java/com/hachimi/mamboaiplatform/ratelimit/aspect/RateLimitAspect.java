package com.hachimi.mamboaiplatform.ratelimit.aspect;

import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.ratelimit.annotation.RateLimit;
import com.hachimi.mamboaiplatform.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;


/**
 * 限流切面 - 支持VIP差异化限流
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 基于用户来限流
     */
    @Resource
    private UserService userService;

    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint point, RateLimit rateLimit) {
        // 获取用户信息用于VIP判断
        User currentUser = getCurrentUser();
        boolean isVip = currentUser != null && userService.isVip(currentUser);

        // 生成限流key，VIP用户和普通用户使用不同的key
        String key = generateRateLimitKey(point, rateLimit, isVip);

        // 根据VIP状态决定限流参数
        int allowedRate;
        String errorMessage;

        if (rateLimit.enableVipDifferentiation() && isVip && rateLimit.vipRate() > 0) {
            // VIP用户使用VIP限制
            allowedRate = rateLimit.vipRate();
            errorMessage = rateLimit.vipMessage();
            log.debug("VIP用户 {} 使用VIP限流策略，允许频率: {}/{} 秒",
                     currentUser.getId(), allowedRate, rateLimit.rateInterval());
        } else {
            // 普通用户使用普通限制
            allowedRate = rateLimit.rate();
            errorMessage = rateLimit.message();
            if (currentUser != null) {
                log.debug("普通用户 {} 使用标准限流策略，允许频率: {}/{} 秒",
                         currentUser.getId(), allowedRate, rateLimit.rateInterval());
            }
        }

        // 使用Redisson的分布式限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.expire(Duration.ofHours(1)); // 1 小时后过期

        // 设置限流器参数：每个时间窗口允许的请求数和时间窗口
        rateLimiter.trySetRate(RateType.OVERALL, allowedRate, rateLimit.rateInterval(), RateIntervalUnit.SECONDS);

        // 尝试获取令牌，如果获取失败则限流
        if (!rateLimiter.tryAcquire(1)) {
            log.warn("限流触发 - Key: {}, 用户: {}, VIP: {}, 允许频率: {}/{} 秒",
                    key, currentUser != null ? currentUser.getId() : "unknown", isVip, allowedRate, rateLimit.rateInterval());
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS_ERROR, errorMessage);
        }
    }

    /**
     * 获取当前用户信息
     */
    private User getCurrentUser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return userService.getLoginUser(request);
            }
        } catch (BusinessException e) {
            // 未登录用户返回null
            log.debug("无法获取当前用户信息，可能未登录: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 生成限流key - 支持VIP差异化
     * @param point 切入点
     * @param rateLimit 限流注解
     * @param isVip 是否为VIP用户
     * @return 限流key
     */
    private String generateRateLimitKey(JoinPoint point, RateLimit rateLimit, boolean isVip) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:");

        // 添加自定义前缀
        if (!rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key()).append(":");
        }

        // 如果启用VIP差异化，在key中区分VIP用户
        if (rateLimit.enableVipDifferentiation()) {
            keyBuilder.append(isVip ? "vip:" : "normal:");
        }

        // 根据限流类型生成不同的key
        switch (rateLimit.limitType()) {
            case API:
                // 接口级别：方法名
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                keyBuilder.append("api:").append(method.getDeclaringClass().getSimpleName())
                        .append(".").append(method.getName());
                break;
            case USER:
                // 用户级别：用户ID
                try {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        User loginUser = userService.getLoginUser(request);
                        keyBuilder.append("user:").append(loginUser.getId());
                    } else {
                        // 无法获取请求上下文，使用IP限流
                        keyBuilder.append("ip:").append(getClientIP());
                    }
                } catch (BusinessException e) {
                    // 未登录用户使用IP限流
                    keyBuilder.append("ip:").append(getClientIP());
                }
                break;
            case IP:
                // IP级别：客户端IP
                keyBuilder.append("ip:").append(getClientIP());
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的限流类型");
        }
        return keyBuilder.toString();
    }

    private String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

}
