package com.hachimi.mamboaiplatform.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    //保证可以序列化

    @Serial
    private static final long serialVersionUID = 114514L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

}