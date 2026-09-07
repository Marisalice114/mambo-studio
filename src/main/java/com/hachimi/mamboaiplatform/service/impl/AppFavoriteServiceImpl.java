package com.hachimi.mamboaiplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.exception.ThrowUtils;
import com.hachimi.mamboaiplatform.mapper.AppFavoriteMapper;
import com.hachimi.mamboaiplatform.model.entity.App;
import com.hachimi.mamboaiplatform.model.entity.AppFavorite;
import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.model.vo.AppVO;
import com.hachimi.mamboaiplatform.service.AppFavoriteService;
import com.hachimi.mamboaiplatform.service.AppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用收藏 服务层实现。
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
@Service
@Slf4j
public class AppFavoriteServiceImpl extends ServiceImpl<AppFavoriteMapper, AppFavorite>
    implements AppFavoriteService {

  @Resource
  private AppService appService;

  @Override
  public boolean addFavorite(Long appId, User loginUser) {
    // 参数校验
    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
    ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
    // 判断应用是否存在
    App app = appService.getById(appId);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
    // 幂等：已收藏则直接返回成功
    if (isFavorite(appId, loginUser.getId())) {
      return true;
    }
    AppFavorite favorite = AppFavorite.builder()
        .userId(loginUser.getId())
        .appId(appId)
        .createTime(LocalDateTime.now())
        .build();
    boolean result = this.save(favorite);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "收藏失败");
    return true;
  }

  @Override
  public boolean cancelFavorite(Long appId, User loginUser) {
    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
    ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
    QueryWrapper queryWrapper = QueryWrapper.create()
        .eq("userId", loginUser.getId())
        .eq("appId", appId);
    boolean result = this.remove(queryWrapper);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "取消收藏失败");
    return true;
  }

  @Override
  public boolean isFavorite(Long appId, Long userId) {
    if (appId == null || userId == null) {
      return false;
    }
    QueryWrapper queryWrapper = QueryWrapper.create()
        .eq("userId", userId)
        .eq("appId", appId);
    return this.count(queryWrapper) > 0;
  }

  @Override
  public Page<AppVO> listFavoriteAppByPage(long pageNum, long pageSize, User loginUser) {
    ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
    // 1. 分页查询当前用户的收藏记录（按收藏时间倒序）
    QueryWrapper favoriteWrapper = QueryWrapper.create()
        .eq("userId", loginUser.getId())
        .orderBy("createTime", false);
    Page<AppFavorite> favoritePage = this.page(Page.of(pageNum, pageSize), favoriteWrapper);
    // 2. 取出 appId 列表
    List<AppFavorite> favoriteList = favoritePage.getRecords();
    List<AppVO> appVOList = new ArrayList<>();
    if (CollUtil.isNotEmpty(favoriteList)) {
      List<Long> appIds = favoriteList.stream()
          .map(AppFavorite::getAppId)
          .collect(Collectors.toList());
      // 3. 批量查询应用（逻辑删除自动过滤）
      QueryWrapper appWrapper = QueryWrapper.create().in("id", appIds);
      List<App> apps = appService.list(appWrapper);
      // 4. 按收藏顺序组装 VO（保留收藏时的顺序）
      appVOList = apps.stream()
          .map(appService::getAppVO)
          .collect(Collectors.toList());
    }
    Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, favoritePage.getTotalRow());
    appVOPage.setRecords(appVOList);
    return appVOPage;
  }
}