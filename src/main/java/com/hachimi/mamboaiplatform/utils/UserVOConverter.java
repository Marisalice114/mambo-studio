package com.hachimi.mamboaiplatform.utils;

import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.model.vo.LoginUserVO;
import com.hachimi.mamboaiplatform.model.vo.UserAdminVO;
import com.hachimi.mamboaiplatform.model.vo.UserDetailVO;
import com.hachimi.mamboaiplatform.model.vo.UserPublicVO;

import java.time.LocalDateTime;

/**
 * 用户VO转换工具类
 * 统一管理所有用户相关的VO转换逻辑
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
public class UserVOConverter {

    /**
     * 转换为登录用户VO
     * 用于用户登录成功后返回基本信息和权限信息
     */
    public static LoginUserVO toLoginUserVO(User user) {
        if (user == null) {
            return null;
        }

        return LoginUserVO.builder()
                .id(user.getId())
                .userAccount(user.getUserAccount())
                .userName(user.getUserName())
                .userAvatar(user.getUserAvatar())
                .userRole(user.getUserRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .isVip(isVipUser(user))
                .vipExpireTime(user.getVipExpireTime())
                .shareCode(user.getShareCode())
                .loginTime(LocalDateTime.now())
                .build();
    }

    /**
     * 转换为用户详细VO
     * 用于个人中心展示，包含脱敏处理的敏感信息
     */
    public static UserDetailVO toUserDetailVO(User user) {
        if (user == null) {
            return null;
        }

        return UserDetailVO.builder()
                .id(user.getId())
                .userAccount(user.getUserAccount())
                .userName(user.getUserName())
                .userAvatar(user.getUserAvatar())
                .userProfile(user.getUserProfile())
                .userRole(user.getUserRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .vipExpireTime(user.getVipExpireTime())
                .vipNumber(user.getVipNumber()) // Ensure vipNumber is converted to String
                .shareCode(user.getShareCode())
                .hasInviter(user.getInviteUser() != null)
                .isVip(isVipUser(user))
                .build();
    }

    /**
     * 转换���用户公开信息VO
     * 用于查看其他用户的公开信息，只包含非敏感数据
     */
    public static UserPublicVO toUserPublicVO(User user) {
        if (user == null) {
            return null;
        }

        return UserPublicVO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .userAvatar(user.getUserAvatar())
                .userProfile(user.getUserProfile())
                .isVip(isVipUser(user))
                .createTime(user.getCreateTime()) // 只显示年月
                .build();
    }

    /**
     * 转换为管理员用户VO
     * 用于后台管理，展示完整的用户信息
     */
    public static UserAdminVO toUserAdminVO(User user) {
        if (user == null) {
            return null;
        }

        return UserAdminVO.builder()
                .id(user.getId())
                .userAccount(user.getUserAccount())
                .userName(user.getUserName())
                .userAvatar(user.getUserAvatar())
                .userRole(user.getUserRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .editTime(user.getEditTime())
                .vipExpireTime(user.getVipExpireTime())
                .vipNumber(user.getVipNumber())
                .inviteUser(user.getInviteUser())
                .isVip(isVipUser(user))
                .build();
    }

    /**
     * 判断用户是否为VIP
     * 使用混合策略：优先检查缓存状态，然后实时校验过期时间
     */
    private static Boolean isVipUser(User user) {
        if (user == null) {
            return false;
        }

        // 第一步：检查缓存的VIP状态
        Boolean cachedVipStatus = user.getIsVip();
        LocalDateTime vipExpireTime = user.getVipExpireTime();

        // 如果没有过期时间，直接返回缓存状态（通常为false）
        if (vipExpireTime == null) {
            return Boolean.TRUE.equals(cachedVipStatus);
        }

        // 第二步：实时校验过期时间
        boolean shouldBeVip = vipExpireTime.isAfter(LocalDateTime.now());

        // 第三步：检查缓存状态与实际状态是否一致
        if (!java.util.Objects.equals(cachedVipStatus, shouldBeVip)) {
            // 注意：这里只记录警告，不直接更新数据库，避免在VO转换时产生副作用
            // 数据库更新由 UserService.isVip() 方法或定时任务处理
            System.out.println("用户 " + user.getId() + " 的VIP状态缓存不一致，缓存：" + cachedVipStatus + "，实际：" + shouldBeVip + "，建议通过定时任务或实时校验更新");
        }

        // 返回实际状态（基于过期时间的实时计算）
        return shouldBeVip;
    }

    /**
     * 脱敏处理VIP编号
     * 保留前2位和后2位，中间用*替代
     */
    private static String maskVipNumber(Long vipNumber) {
        if (vipNumber == null) {
            return null;
        }
        String vipStr = vipNumber.toString();
        if (vipStr.length() <= 4) {
            return vipStr;
        }
        return vipStr.substring(0, 2) + "****" + vipStr.substring(vipStr.length() - 2);
    }
}
