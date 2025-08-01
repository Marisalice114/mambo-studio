package com.hachimi.mamboaiplatform.utils;

/**
 * 数据脱敏工具类
 */
public class DataMaskingUtil {

    /**
     * 手机号脱敏：保留前3位和后4位
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏：保留前2位和@后的域名
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return username + "***@" + domain;
        }
        return username.substring(0, 2) + "***@" + domain;
    }

    /**
     * 会员编号脱敏：保留前2位和后2位
     */
    public static String maskVipNumber(Long vipNumber) {
        if (vipNumber == null) {
            return null;
        }
        String str = vipNumber.toString();
        if (str.length() <= 4) {
            return "****";
        }
        return str.substring(0, 2) + "****" + str.substring(str.length() - 2);
    }

    /**
     * 智能账号脱敏：根据格式判断是手机号还是邮箱
     */
    public static String maskAccount(String account) {
        if (account == null) {
            return null;
        }

        // 判断是否为邮箱
        if (account.contains("@")) {
            return maskEmail(account);
        }

        // 判断是否为手机号（11位数字）
        if (account.matches("^1[3-9]\\d{9}$")) {
            return maskPhone(account);
        }

        // 其他格式，保留前2位
        if (account.length() <= 4) {
            return "****";
        }
        return account.substring(0, 2) + "****";
    }
}
