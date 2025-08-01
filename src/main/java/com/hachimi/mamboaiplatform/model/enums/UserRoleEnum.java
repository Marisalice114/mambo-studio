package com.hachimi.mamboaiplatform.model.enums;


import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin"),
    VIP("VIP用户", "vip");

    private final String text;
    private final String value;

    // 静态Map缓存，类加载时初始化
    private static final Map<String, UserRoleEnum> VALUE_MAP = new HashMap<>();

    static {
        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
            VALUE_MAP.put(roleEnum.value, roleEnum);
        }
    }

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举（O(1)时间复杂度）
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        return VALUE_MAP.get(value);
    }
}

