package com.hachimi.mamboaiplatform.model.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * 用户ID（必需）
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    /**
     * VIP过期时间
     */
    private LocalDateTime vipExpireTime;

    /**
     * 会员编号
     */
    private Long vipNumber;

    /**
     * 邀请用户ID
     */
    private Long inviteUser;

    private static final long serialVersionUID = 1L;
}
