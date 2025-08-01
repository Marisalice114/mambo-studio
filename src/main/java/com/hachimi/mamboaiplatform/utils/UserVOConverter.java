package com.hachimi.mamboaiplatform.utils;

import com.hachimi.mamboaiplatform.entity.User;
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
     * 用于���户登录成功后返回基本信息和权限信息
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
     * 用于个人中心展示，用户查看自己的完整信息（不脱敏）
     */
    public static UserDetailVO toUserDetailVO(User user) {
        if (user == null) {
            return null;
        }

        return UserDetailVO.builder()
                .id(user.getId())
                .userAccount(user.getUserAccount()) // 个人查看自己完整账号
                .userName(user.getUserName())
                .userAvatar(user.getUserAvatar())
                .userProfile(user.getUserProfile())
                .userRole(user.getUserRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .vipExpireTime(user.getVipExpireTime())
                .vipNumber(user.getVipNumber()) // 个人查看自己完整VIP编号，不脱敏
                .shareCode(user.getShareCode())
                .hasInviter(user.getInviteUser() != null)
                .isVip(isVipUser(user))
                .build();
    }

    /**
     * 转换为用户公开信息VO
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
     * 用于后台管理，展示用户信息但不包含密码
     */
    public static UserAdminVO toUserAdminVO(User user) {
        if (user == null) {
            return null;
        }

        return UserAdminVO.builder()
                .id(user.getId())
                .userAccount(user.getUserAccount()) // 管理员可见用户账号
                .userName(user.getUserName())
                .userAvatar(user.getUserAvatar())
                .userRole(user.getUserRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .editTime(user.getEditTime())
                .vipExpireTime(user.getVipExpireTime())
                .vipNumber(user.getVipNumber()) // 管理员可见完整VIP编号
                .inviteUser(user.getInviteUser())
                .isVip(isVipUser(user))
                .build();
    }

    /**
     * 判断用户是否为VIP
     */
    private static Boolean isVipUser(User user) {
        if (user == null || user.getVipExpireTime() == null) {
            return false;
        }
        return user.getVipExpireTime().isAfter(LocalDateTime.now());
    }
}
