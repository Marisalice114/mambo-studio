package com.hachimi.mamboaiplatform.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hachimi.mamboaiplatform.annotation.AuthCheck;
import com.hachimi.mamboaiplatform.common.BaseResponse;
import com.hachimi.mamboaiplatform.common.DeleteRequest;
import com.hachimi.mamboaiplatform.common.ResultUtils;
import com.hachimi.mamboaiplatform.constant.AppConstant;
import com.hachimi.mamboaiplatform.constant.UserConstant;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.exception.ThrowUtils;
import com.hachimi.mamboaiplatform.model.dto.app.*;
import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.model.vo.AppVO;
import com.hachimi.mamboaiplatform.ratelimit.annotation.RateLimit;
import com.hachimi.mamboaiplatform.ratelimit.enums.RateLimitType;
import com.hachimi.mamboaiplatform.service.AppService;
import com.hachimi.mamboaiplatform.service.ProjectDownloadService;
import com.hachimi.mamboaiplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import com.hachimi.mamboaiplatform.model.entity.App;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

  @Resource
  private AppService appService;

  @Resource
  private UserService userService;

  @Resource
  private ProjectDownloadService projectDownloadService;

  /**
   * 应用聊天生成代码（流式 SSE）
   *
   * @param appId   应用 ID
   * @param message 用户消息
   * @param request 请求对象
   * @return 生成结果流
   */
  @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @RateLimit(limitType = RateLimitType.USER, rate = 5, vipRate = 20, rateInterval = 60, enableVipDifferentiation = true, message = "AI对话请求过于频繁，请稍后再试。升级VIP可享有更高频率限制", vipMessage = "VIP用户AI对话请求过于频繁，请稍后再试")
  public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
      @RequestParam String message,
      HttpServletRequest request) {
    // 参数校验
    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
    ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
    // 获取当前登录用户
    User loginUser = userService.getLoginUser(request);
    // 调用服务生成代码（流式）
    Flux<String> stringFlux = appService.chatToGenCode(appId, message, loginUser);
    // 对这个流式结果进行一层封装，防止空格的丢失
    return stringFlux.map(chunk -> {
      // 快速构造一个map
      Map<String, String> resultMap = Map.of("d", chunk);
      String jsonData = JSONUtil.toJsonStr(resultMap);
      return ServerSentEvent.<String>builder()
          .data(jsonData)
          .build();
    });
  }

  /**
   * 创建应用
   *
   * @param appAddRequest 应用创建请求
   * @param request       请求对象
   * @return 新应用的 ID
   */
  @PostMapping("/add")
  @RateLimit(limitType = RateLimitType.USER, rate = 5, // 普通用户每小时5次
      vipRate = 20, // VIP用户每小时20次
      rateInterval = 3600, enableVipDifferentiation = true, message = "创建应用过于频繁，普通用户每小时最多创建5个应用", vipMessage = "创建应用过于频繁，VIP用户每小时最多创建20个应用")
  public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
    // 获取当前登录用户
    User loginUser = userService.getLoginUser(request);
    Long appId = appService.createApp(appAddRequest, loginUser);
    return ResultUtils.success(appId);
  }

  /**
   * 更新应用（用户只能更新自己的应用名称）
   *
   * @param appUpdateRequest 更新请求
   * @param request          请求
   * @return 更新结果
   */
  @PostMapping("/update")
  @RateLimit(limitType = RateLimitType.USER, rate = 20, rateInterval = 3600, message = "应用更新过于频繁，每小时最多更新20次")
  public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
    if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    long id = appUpdateRequest.getId();
    // 判断是否存在
    App oldApp = appService.getById(id);
    ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
    // 仅本人可更新
    if (!oldApp.getUserId().equals(loginUser.getId())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }
    App app = new App();
    app.setId(id);
    app.setAppName(appUpdateRequest.getAppName());
    // 设置编辑时间
    app.setEditTime(LocalDateTime.now());
    boolean result = appService.updateById(app);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(true);
  }

  /**
   * 删除应用（用户只能删除自己的应用）
   *
   * @param deleteRequest 删除请求
   * @param request       请求
   * @return 删除结果
   */
  @PostMapping("/delete")
  @RateLimit(limitType = RateLimitType.USER, rate = 5, rateInterval = 3600, message = "应用删除过于频繁，每小时最多删除5个应用")
  public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
    if (deleteRequest == null || deleteRequest.getId() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    long id = deleteRequest.getId();
    // 判断是否存在
    App oldApp = appService.getById(id);
    ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
    // 仅本人或管理员可删除
    if (!oldApp.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }
    boolean result = appService.removeById(id);
    return ResultUtils.success(result);
  }

  /**
   * 根据 id 获取应用详情
   *
   * @param id 应用 id
   * @return 应用详情
   */
  @GetMapping("/get/vo")
  public BaseResponse<AppVO> getAppVOById(long id) {
    ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
    // 查询数据库
    App app = appService.getById(id);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
    // 获取封装类（包含用户信息）
    return ResultUtils.success(appService.getAppVO(app));
  }

  /**
   * 分页获取当前用户创建的应用列表
   *
   * @param appQueryRequest 查询请求
   * @param request         请求
   * @return 应用列表
   */
  @PostMapping("/my/list/page/vo")
  public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest,
      HttpServletRequest request) {
    ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
    User loginUser = userService.getLoginUser(request);
    // 限制每页最多 20 个
    long pageSize = appQueryRequest.getPageSize();
    ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
    long pageNum = appQueryRequest.getPageNum();
    // 只查询当前用户的应用
    appQueryRequest.setUserId(loginUser.getId());
    QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
    Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
    // 数据封装
    Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
    List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
    appVOPage.setRecords(appVOList);
    return ResultUtils.success(appVOPage);
  }

  /**
   * 分页获取精选应用列表
   *
   * @param appQueryRequest 查询请求
   * @return 精选应用列表
   */
  @PostMapping("/good/list/page/vo")
  @Cacheable(value = "good_app_page", key = "T(com.hachimi.mamboaiplatform.utils.CacheKeyUtil).generateKey(#appQueryRequest)", condition = "#appQueryRequest.pageNum <= 10") // 仅缓存前
                                                                                                                                                                             // 10
                                                                                                                                                                             // 页
  public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
    ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
    // 限制每页最多 20 个
    long pageSize = appQueryRequest.getPageSize();
    ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
    long pageNum = appQueryRequest.getPageNum();
    // 只查询精选的应用
    appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
    QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
    // 分页查询
    Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
    // 数据封装
    Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
    List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
    appVOPage.setRecords(appVOList);
    return ResultUtils.success(appVOPage);
  }

  /**
   * 管理员删除应用
   *
   * @param deleteRequest 删除请求
   * @return 删除结果
   */
  @PostMapping("/admin/delete")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
    if (deleteRequest == null || deleteRequest.getId() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    long id = deleteRequest.getId();
    // 判断是否存在
    App oldApp = appService.getById(id);
    ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
    boolean result = appService.removeById(id);
    return ResultUtils.success(result);
  }

  /**
   * 管理员更新应用
   *
   * @param appAdminUpdateRequest 更新���求
   * @return 更新结果
   */
  @PostMapping("/admin/update")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
    if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    long id = appAdminUpdateRequest.getId();
    // 判断是否存在
    App oldApp = appService.getById(id);
    ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
    App app = new App();
    BeanUtil.copyProperties(appAdminUpdateRequest, app);
    // 设置编辑时间
    app.setEditTime(LocalDateTime.now());
    boolean result = appService.updateById(app);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(true);
  }

  /**
   * 管理员分页获取应用列表
   *
   * @param appQueryRequest 查询请��
   * @return 应用列表
   */
  @PostMapping("/admin/list/page/vo")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
    ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
    long pageNum = appQueryRequest.getPageNum();
    long pageSize = appQueryRequest.getPageSize();
    QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
    Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
    // 数据封装
    Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
    List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
    appVOPage.setRecords(appVOList);
    return ResultUtils.success(appVOPage);
  }

  /**
   * 管理员根据 id 获取应用详情
   *
   * @param id 应用 id
   * @return 应用详情
   */
  @GetMapping("/admin/get/vo")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<AppVO> getAppVOByIdByAdmin(long id) {
    ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
    // 查询数据库
    App app = appService.getById(id);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
    // 获取封装类
    return ResultUtils.success(appService.getAppVO(app));
  }

  /**
   * 下载应用代码
   *
   * @param appId    应用ID
   * @param request  请求
   * @param response 响应
   */
  @GetMapping("/download/{appId}")
  @RateLimit(limitType = RateLimitType.USER, rate = 3, vipRate = 10, rateInterval = 3600, enableVipDifferentiation = true, message = "代码下载过于频繁，每小时最多下载3次。升级VIP可享有更高限额", vipMessage = "VIP用户代码下载过于频繁，每小时最多下载10次")
  public void downloadAppCode(@PathVariable Long appId,
      HttpServletRequest request,
      HttpServletResponse response) {
    // 1. 基础校验
    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
    // 2. 查询应用信息
    App app = appService.getById(appId);
    ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
    // 3. 权限校验：应用创建者可以下载自己的应用，VIP用户可以下载精选应用
    User loginUser = userService.getLoginUser(request);
    boolean isAppOwner = app.getUserId().equals(loginUser.getId());
    boolean isVipUser = userService.isVip(loginUser);
    boolean isGoodApp = AppConstant.GOOD_APP_PRIORITY.equals(app.getPriority());

    // 权限判断：应用创建者可以下载自己的应用，VIP用户可以下载精选应用
    if (!isAppOwner && !(isVipUser && isGoodApp)) {
      if (!isVipUser && isGoodApp) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "精选应用仅VIP用户可下载，请先升级VIP");
      } else {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下载该应用代码");
      }
    }
    // 4. 构建应用代码目录路径（生成目录，非部署目录）
    String codeGenType = app.getCodeGenType();
    String sourceDirName = codeGenType + "_" + appId;
    String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
    // 5. 检查代码目录是否存在
    File sourceDir = new File(sourceDirPath);
    ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
        ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
    // 6. 生成下载文件名（不建议添加中文内容）
    String downloadFileName = String.valueOf(appId);
    // 7. 调用通用下载服务
    projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
  }

  /**
   * 部署应用
   * 
   * @param appDeployRequest 部署请求
   * @param request          HTTP请求
   * @return 部署后的访问URL
   */
  @PostMapping("/deploy")
  @RateLimit(limitType = RateLimitType.USER, rate = 5, vipRate = 20, rateInterval = 3600, enableVipDifferentiation = true, message = "应用部署过于频繁，每小时最多部署5次。升级VIP可享有更高限额", vipMessage = "VIP用户应用部署过于频繁，每小时最多部署20次")
  public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
    // 1. 参数校验
    ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
    Long appId = appDeployRequest.getAppId();
    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

    // 2. 获取登录用户
    User loginUser = userService.getLoginUser(request);

    // 3. 调用服务层部署方法
    String deployUrl = appService.deployApp(appId, loginUser);

    return ResultUtils.success(deployUrl);
  }

}
