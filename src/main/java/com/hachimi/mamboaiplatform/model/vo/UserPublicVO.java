package com.hachimi.mamboaiplatform.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户公开信息VO - 用于查看其他用户的公开信息
 * 只包含可以公开展示的字段，保护隐私信息
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
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
     * 是否为VIP用户（显示身份标识）
     */
    private Boolean isVip;

    /**
     * 注册时间（只显示年月，不显示具体时间）
     */
    @JsonFormat(pattern = "yyyy-MM")
    private LocalDateTime createTime;
}
