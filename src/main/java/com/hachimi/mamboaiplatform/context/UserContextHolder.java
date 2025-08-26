package com.hachimi.mamboaiplatform.context;

import com.hachimi.mamboaiplatform.model.entity.User;

/**
 * 保存当前请求用户的轻量上下文，供异步/回调线程获取，避免再次依赖 HttpSession。
 * 最小实现：ThreadLocal；如需跨线程池或虚拟线程，可改为 InheritableThreadLocal。
 */
public class UserContextHolder {

    private static final InheritableThreadLocal<User> CTX = new InheritableThreadLocal<>();

    public static void set(User user) {
        CTX.set(user);
    }

    public static User get() {
        return CTX.get();
    }

    public static void clear() {
        CTX.remove();
    }
}
