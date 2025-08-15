package com.hachimi.mamboaiplatform.common;

import lombok.Data;

import java.io.Serializable;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import lombok.NoArgsConstructor;

/**
 * 基础响应封装
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

