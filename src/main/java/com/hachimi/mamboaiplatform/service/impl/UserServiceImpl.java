package com.hachimi.mamboaiplatform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.hachimi.mamboaiplatform.entity.User;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.mapper.UserMapper;
import com.hachimi.mamboaiplatform.model.dto.user.UserQueryRequest;
import com.hachimi.mamboaiplatform.model.enums.UserRoleEnum;
import com.hachimi.mamboaiplatform.model.vo.LoginUserVO;
import com.hachimi.mamboaiplatform.model.vo.UserAdminVO;
import com.hachimi.mamboaiplatform.model.vo.UserDetailVO;
import com.hachimi.mamboaiplatform.model.vo.UserPublicVO;
import com.hachimi.mamboaiplatform.service.UserService;
import com.hachimi.mamboaiplatform.utils.UserVOConverter;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.hachimi.mamboaiplatform.constant.UserConstant.USER_LOGIN_STATE;
import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //2.检查是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        //mybatis-flex的逻辑是,构建queryWrapper后,调用相应方法来执行查询
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号已存在");
        }
        //3.加密密码
        String encryptPassword = getEncryptedPassword(userPassword);
        //4.插入数据
        String userName = "mambo_" + UUID.randomUUID().toString().substring(0, 8);
        User user = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptPassword)
                .userName(userName)
                .userRole(UserRoleEnum.USER.getValue())
                .build();
        //5.保存
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户注册失败");
        }
        return user.getId();
    }

    private String getEncryptedPassword(String userPassword) {
        //加密
        //设置盐值，来混淆密码
        final String SALT = "mambo_1919810";
//        return BCrypt.hashpw(userPassword, salt); //需要按照规则来指定盐值
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 获取登录用户VO
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        return UserVOConverter.toLoginUserVO(user);
    }

    /**
     * 转换为用户详细VO（个人中心版本）
     */
    @Override
    public UserDetailVO getUserDetailVO(User user) {
        return UserVOConverter.toUserDetailVO(user);
    }

    /**
     * 转换为管理员VO（后台管理版本）
     */
    @Override
    public UserAdminVO getUserAdminVO(User user) {
        return UserVOConverter.toUserAdminVO(user);
    }

    /**
     * 转换为用户公开信息VO（查看其他用户信息版本）
     */
    @Override
    public UserPublicVO getUserPublicVO(User user) {
        return UserVOConverter.toUserPublicVO(user);
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptedPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        // 这里相当于是给session开辟了一块完整的存储空间，来任意存储���息
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 4. 获得脱敏后的用户信息
        return this.getLoginUserVO(user);
    }


    /**
     * 获取登录用户信息
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        //异常处理
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        //数据库检测
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return currentUser;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        //异常处理
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        // 清除用户登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取查询条件包装器
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        Boolean isVip = userQueryRequest.getIsVip();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        // 创建基础查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                // MyBatis-Flex会自动忽略null值，无需手动判断
                .where(column("id").eq(id))
                .and(column("userRole").eq(userRole))
                .and(column("userAccount").like(userAccount))
                .and(column("userName").like(userName))
                .and(column("userProfile").like(userProfile));

        // VIP状态查询
        if (isVip != null) {
            if (isVip) {
                // 查询VIP用户：vipExpireTime不为空且大于当前时间
                queryWrapper.and(column("vipExpireTime").isNotNull())
                           .and(column("vipExpireTime").gt(LocalDateTime.now()));
            } else {
                // 查询非VIP用户：vipExpireTime为空或已过期
                queryWrapper.and((Consumer<QueryWrapper>) wrapper ->
                    wrapper.where(column("vipExpireTime").isNull())
                           .or(column("vipExpireTime").le(LocalDateTime.now()))
                );
            }
        }

        // 排序处理 - 链式调用
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equals(sortOrder);
            queryWrapper.orderBy(column(sortField), isAsc);
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderBy(column("createTime"), false);
        }

        return queryWrapper;
    }

    /**
     * 获取加密密码（公开方法供Controller使用）
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        return getEncryptedPassword(userPassword);
    }

    /**
     * 批量转换为用户公开信息VO列表
     */
    @Override
    public List<UserPublicVO> getUserPublicVOList(List<User> userList) {
        if (userList == null || userList.isEmpty()) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserPublicVO)
                .collect(Collectors.toList());
    }
}