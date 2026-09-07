package com.hachimi.mamboaiplatform.service;

import com.hachimi.mamboaiplatform.model.entity.AppFavorite;
import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 应用收藏 服务层。
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
public interface AppFavoriteService extends IService<AppFavorite> {

    /**
     * 收藏应用（幂等：已收藏则忽略）
     *
     * @param appId     应用 id
     * @param loginUser 当前登录用户
     * @return 是否成功
     */
    boolean addFavorite(Long appId, User loginUser);

    /**
     * 取消收藏
     *
     * @param appId     应用 id
     * @param loginUser 当前登录用户
     * @return 是否成功
     */
    boolean cancelFavorite(Long appId, User loginUser);

    /**
     * 判断用户是否已收藏某应用
     *
     * @param appId     应用 id
     * @param userId    用户 id
     * @return 是否已收藏
     */
    boolean isFavorite(Long appId, Long userId);

    /**
     * 分页获取当前用户收藏的应用列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param loginUser 当前登录用户
     * @return 应用列表分页
     */
    Page<AppVO> listFavoriteAppByPage(long pageNum, long pageSize, User loginUser);
}