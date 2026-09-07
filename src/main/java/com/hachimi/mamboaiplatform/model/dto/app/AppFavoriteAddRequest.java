package com.hachimi.mamboaiplatform.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 收藏应用请求
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
@Data
public class AppFavoriteAddRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}