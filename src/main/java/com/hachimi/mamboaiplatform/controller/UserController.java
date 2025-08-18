package com.hachimi.mamboaiplatform.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hachimi.mamboaiplatform.annotation.AuthCheck;
import com.hachimi.mamboaiplatform.common.BaseResponse;

import com.hachimi.mamboaiplatform.common.ResultUtils;
import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.exception.ThrowUtils;
import com.hachimi.mamboaiplatform.model.dto.user.UserLoginRequest;
import com.hachimi.mamboaiplatform.model.dto.user.UserRegisterRequest;
import com.hachimi.mamboaiplatform.model.dto.user.UserAddRequest;
import com.hachimi.mamboaiplatform.model.dto.user.UserUpdateRequest;
import com.hachimi.mamboaiplatform.model.dto.user.UserQueryRequest;
import com.hachimi.mamboaiplatform.model.vo.LoginUserVO;
import com.hachimi.mamboaiplatform.model.vo.UserAdminVO;
import com.hachimi.mamboaiplatform.model.vo.UserDetailVO;
import com.hachimi.mamboaiplatform.model.vo.UserPublicVO;
import com.hachimi.mamboaiplatform.ratelimit.annotation.RateLimit;
import com.hachimi.mamboaiplatform.ratelimit.enums.RateLimitType;
import com.hachimi.mamboaiplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hachimi.mamboaiplatform.constant.UserConstant.*;

/**
 * 用户 控制层。
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
@RestController
@RequestMapping("/user")
public class UserController {

  @Resource
  private UserService userService;

  // ========== 管理员专用接口 - 使用 UserAdminVO ==========

  /**
   * 保存用户 - 管理员权限
   */
  @PostMapping("save")
  @AuthCheck(mustRole = ADMIN_ROLE)
  public BaseResponse<Boolean> save(@RequestBody User user) {
    boolean result = userService.save(user);
    return ResultUtils.success(result);
  }

  /**
   * 更新用户 - 管理员权限
   */
  @PutMapping("update")
  @AuthCheck(mustRole = ADMIN_ROLE)
  public BaseResponse<Boolean> update(@RequestBody User user) {
    boolean result = userService.updateById(user);
    return ResultUtils.success(result);
  }

  /**
   * 查询所有用户 - 管理员权限
   * 使用 UserAdminVO 返回管理员需要的完整信息
   */
  @GetMapping("list")
  @AuthCheck(mustRole = ADMIN_ROLE)
  public BaseResponse<List<UserAdminVO>> list() {
    List<User> userList = userService.list();
    List<UserAdminVO> userAdminVOList = userList.stream()
        .map(userService::getUserAdminVO) // 需要在UserService中实现此方法
        .toList();
    return ResultUtils.success(userAdminVOList);
  }

  /**
   * 分页查询用户 - 管理员权限
   * 使用 UserAdminVO 返回管理员版本的分页数据
   */
  @GetMapping("page")
  @AuthCheck(mustRole = ADMIN_ROLE)
  public BaseResponse<Page<UserAdminVO>> page(Page<User> page) {
    Page<User> userPage = userService.page(page);
    Page<UserAdminVO> userAdminVOPage = convertToAdminVOPage(userPage);
    return ResultUtils.success(userAdminVOPage);
  }

  // ========== 需要权限检查的接口 ==========

  /**
   * 根据ID获取用户信息 - 智能权限判断
   * 根据访问者身份返回不同级别���信息：
   * 1. 查看自己 -> UserDetailVO (详细信息)
   * 2. 管理员查他人 -> UserAdminVO (管理员视图)
   * 3. 普通用户查他人 -> UserPublicVO (公开信息，需要新建)
   */
  @GetMapping("getInfo/{id}")
  @AuthCheck(mustRole = DEFAULT_ROLE) // 至少需要登录
  public BaseResponse<Object> getInfo(@PathVariable Long id, HttpServletRequest request) {
    User currentUser = userService.getLoginUser(request);
    User targetUser = userService.getById(id);
    ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR);

    // 情况1：查看自己的信息 -> 返回详细信息
    if (currentUser.getId().equals(id)) {
      UserDetailVO userDetailVO = userService.getUserDetailVO(targetUser);
      return ResultUtils.success(userDetailVO);
    }

    // 情况2：管理员查看他人信息 -> 返回管理员视图
    if (ADMIN_ROLE.equals(currentUser.getUserRole())) {
      UserAdminVO userAdminVO = userService.getUserAdminVO(targetUser);
      return ResultUtils.success(userAdminVO);
    }

    // 情况3：普通用户查看他人信息 -> 返回公开信息
    UserPublicVO publicInfo = userService.getUserPublicVO(targetUser);
    return ResultUtils.success(publicInfo);
  }

  // ========== 公开接口（无需登录）==========

  /**
   * 用户注册 - 公开接口
   */
  @PostMapping("register")
  @RateLimit(limitType = RateLimitType.IP, rate = 5, rateInterval = 3600, message = "注册过于频繁，每小时最多注册5次")
  public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
    ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
    String userAccount = userRegisterRequest.getUserAccount();
    String userPassword = userRegisterRequest.getUserPassword();
    String checkPassword = userRegisterRequest.getCheckPassword();
    long result = userService.userRegister(userAccount, userPassword, checkPassword);
    return ResultUtils.success(result);
  }

  /**
   * 用户登录 - 公开接口
   * 注意：这里移除了错误的权限注解
   */
  @PostMapping("/login")
  @RateLimit(limitType = RateLimitType.IP, rate = 10, rateInterval = 300, message = "登录尝试过于频繁，5分钟内最多尝试10次")
  public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
      HttpServletRequest request) {
    ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
    String userAccount = userLoginRequest.getUserAccount();
    String userPassword = userLoginRequest.getUserPassword();
    LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
    return ResultUtils.success(loginUserVO);
  }

  // ========== 需要登录的接口 ==========

  /**
   * 获取当前登录用户信息 - 需要登录
   * 使用 LoginUserVO 返回登录用户信息
   */
  @GetMapping("/getLoginUser")
  @AuthCheck(mustRole = DEFAULT_ROLE)
  public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
    LoginUserVO loginUserVO = userService.getLoginUserVO(loginUser);
    return ResultUtils.success(loginUserVO);
  }

  /**
   * 获取个人详细信息 - 需要登录
   * 使用 UserDetailVO 返回用户查看自己的详细信息
   */
  @GetMapping("/me/detail")
  @AuthCheck(mustRole = DEFAULT_ROLE)
  public BaseResponse<UserDetailVO> getMyDetail(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
    UserDetailVO userDetailVO = userService.getUserDetailVO(loginUser);
    return ResultUtils.success(userDetailVO);
  }

  /**
   * 用户注销 - 需要���录
   */
  @PostMapping("/logout")
  @AuthCheck(mustRole = DEFAULT_ROLE)
  public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
    ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
    boolean result = userService.userLogout(request);
    return ResultUtils.success(result);
  }

  // ========== VIP相关接口示例 ==========

  /**
   * VIP专属功能 - 需要VIP权限
   */
  @GetMapping("/vip/features")
  @AuthCheck(mustRole = VIP_ROLE)
  public BaseResponse<String> getVipFeatures() {
    return ResultUtils.success("VIP专属功能");
  }

  // ========== 新增的完整CRUD接口 ==========

  /**
   * 创建用户 - 管理员权限
   */
  @PostMapping("/add")
  @AuthCheck(mustRole = ADMIN_ROLE)
  public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
    ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
    User user = new User();
    BeanUtil.copyProperties(userAddRequest, user);
    // 默认密码 12345678
    final String DEFAULT_PASSWORD = "12345678";
    String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
    user.setUserPassword(encryptPassword);
    boolean result = userService.save(user);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(user.getId());
  }

  /**
   * 根据 id 获取用户（仅管理员）
   */
  @GetMapping("/get")
  @AuthCheck(mustRole = ADMIN_ROLE)
  public BaseResponse<User> getUserById(long id) {
    ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
    User user = userService.getById(id);
    ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
    return ResultUtils.success(user);
  }

  /**
   * 根据 id 获取用户公开信息VO
   */
  @GetMapping("/get/vo")
  public BaseResponse<UserPublicVO> getUserVOById(long id) {
    ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
    User user = userService.getById(id);
    ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
    UserPublicVO userPublicVO = userService.getUserPublicVO(user);
    return ResultUtils.success(userPublicVO);
  }

  /**
   * 用户删除自己的账号 - 需要登录
   * 用户可以删除自己的账号（逻辑删除）
   */
  @PostMapping("/delete")
  @AuthCheck(mustRole = DEFAULT_ROLE)
  public BaseResponse<Boolean> deleteMyAccount(HttpServletRequest request) {
    // 获取当前登录用户
    User loginUser = userService.getLoginUser(request);
    ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

    // 用户删除自己的账号（逻辑删除）
    boolean result = userService.removeById(loginUser.getId());

    // 删除成功后清除登录状态
    if (result) {
      userService.userLogout(request);
    }

    return ResultUtils.success(result);
  }

  /**
   * 管理员删除用户 - 管理员权限
   * 管理员可以删除任意用户（逻辑删除）
   */
  @DeleteMapping("remove/{id}")
  @AuthCheck(mustRole = ADMIN_ROLE)
  public BaseResponse<Boolean> adminDeleteUser(@PathVariable Long id) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
    }
    // 逻辑删除：只标记isDelete=1，保留数据
    boolean result = userService.removeById(id);
    return ResultUtils.success(result);
  }

  /**
   * 更新用户 - 管理员权限
   */
  @PostMapping("/update")
  @AuthCheck(mustRole = ADMIN_ROLE)
  public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
    if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User user = new User();
    BeanUtil.copyProperties(userUpdateRequest, user);
    boolean result = userService.updateById(user);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(true);
  }

  /**
   * 分页获取用户封装列表（仅管理员）
   */
  @PostMapping("/list/page/vo")
  @AuthCheck(mustRole = ADMIN_ROLE)
  public BaseResponse<Page<UserAdminVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
    ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
    long pageNum = userQueryRequest.getPageNum();
    long pageSize = userQueryRequest.getPageSize();
    Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
        userService.getQueryWrapper(userQueryRequest));
    // 转换为管理员VO
    Page<UserAdminVO> userAdminVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
    List<UserAdminVO> userAdminVOList = userPage.getRecords().stream()
        .map(userService::getUserAdminVO)
        .toList();
    userAdminVOPage.setRecords(userAdminVOList);
    return ResultUtils.success(userAdminVOPage);
  }

  /**
   * 分页获取用户公开信息列表（需要登录）
   */
  @PostMapping("/list/page/public")
  @AuthCheck(mustRole = DEFAULT_ROLE)
  public BaseResponse<Page<UserPublicVO>> listUserPublicByPage(@RequestBody UserQueryRequest userQueryRequest) {
    ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
    long pageNum = userQueryRequest.getPageNum();
    long pageSize = userQueryRequest.getPageSize();
    Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
        userService.getQueryWrapper(userQueryRequest));
    // 转换为公开信息VO
    Page<UserPublicVO> userPublicVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
    List<UserPublicVO> userPublicVOList = userService.getUserPublicVOList(userPage.getRecords());
    userPublicVOPage.setRecords(userPublicVOList);
    return ResultUtils.success(userPublicVOPage);
  }

  // ========== 工具方法 ==========

  /**
   * 转换为管理员VO分页对象
   */
  private Page<UserAdminVO> convertToAdminVOPage(Page<User> userPage) {
    Page<UserAdminVO> adminVOPage = new Page<>();
    adminVOPage.setPageNumber(userPage.getPageNumber());
    adminVOPage.setPageSize(userPage.getPageSize());
    adminVOPage.setTotalRow(userPage.getTotalRow());
    adminVOPage.setTotalPage(userPage.getTotalPage());

    List<UserAdminVO> adminVOList = userPage.getRecords().stream()
        .map(userService::getUserAdminVO)
        .toList();
    adminVOPage.setRecords(adminVOList);

    return adminVOPage;
  }
}
