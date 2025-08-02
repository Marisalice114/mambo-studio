package com.hachimi.mamboaiplatform.service;

import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.model.dto.user.UserQueryRequest;
import com.hachimi.mamboaiplatform.model.vo.LoginUserVO;
import com.hachimi.mamboaiplatform.model.vo.UserAdminVO;
import com.hachimi.mamboaiplatform.model.vo.UserDetailVO;
import com.hachimi.mamboaiplatform.model.vo.UserPublicVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);



    /**
     * 获取登录的用户信息
     *
     * @param request
     * @return 是否成功
     */
    User getLoginUser(HttpServletRequest request);




    /**
     * 用户注销
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 转换为用户详细VO
     */
    UserDetailVO getUserDetailVO(User user);

    /**
     * 转换为管理员VO
     */
    UserAdminVO getUserAdminVO(User user);

    /**
     * 转换为用户公开信息VO
     */
    UserPublicVO getUserPublicVO(User user);

    /**
     * 获取查询条件包装器
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取加密密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 批量转换为用户公开信息VO列表
     */
    List<UserPublicVO> getUserPublicVOList(List<User> userList);
}
