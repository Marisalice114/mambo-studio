package com.hachimi.mamboaiplatform.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用收藏 实体类。
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app_favorite")
public class AppFavorite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 收藏用户id
     */
    @Column("userId")
    private Long userId;

    /**
     * 被收藏的应用id
     */
    @Column("appId")
    private Long appId;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

}