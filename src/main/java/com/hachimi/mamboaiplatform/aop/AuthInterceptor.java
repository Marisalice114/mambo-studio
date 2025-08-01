package com.hachimi.mamboaiplatform.aop;


import com.hachimi.mamboaiplatform.annotation.AuthCheck;
import com.hachimi.mamboaiplatform.entity.User;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

import static com.hachimi.mamboaiplatform.constant.UserConstant.*;


@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();

        // 获取当前请求和用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        User loginUser;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (Exception e) {
            // 未登录，但需要权限，拒绝访问
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 根据需要的权限级别进行检查
        switch (mustRole) {
            case DEFAULT_ROLE:
                // user权限 = 登录即可，任何登录用户都有user权限
                // 已经通过上面的getLoginUser检查，直接放行
                break;

            case VIP_ROLE:
                // vip权限 = VIP用户 或 管理员
                if (!hasVipPermission(loginUser)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要VIP权限");
                }
                break;

            case ADMIN_ROLE:
                // admin权限 = 管理员
                if (!hasAdminPermission(loginUser)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要管理员权限");
                }
                break;

            default:
                // 未知权限类型
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "未知的权限类型: " + mustRole);
        }

        return joinPoint.proceed();
    }

    /**
     * 检查VIP权限
     * VIP权限：VIP用户且未过期，或者管理员
     */
    private boolean hasVipPermission(User user) {
        // 管理员拥有所有权限
        if (hasAdminPermission(user)) {
            return true;
        }

        // 检查是否为VIP用户且未过期
        if (user.getVipExpireTime() == null) {
            return false;
        }
        return user.getVipExpireTime().isAfter(LocalDateTime.now());
    }

    /**
     * 检查管理员权限
     */
    private boolean hasAdminPermission(User user) {
        return ADMIN_ROLE.equals(user.getUserRole());
    }

}
