package com.hachimi.mamboaiplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hachimi.mamboaiplatform.core.AiCodeGeneratorFacade;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.exception.ThrowUtils;
import com.hachimi.mamboaiplatform.mapper.AppMapper;
import com.hachimi.mamboaiplatform.model.dto.app.AppQueryRequest;
import com.hachimi.mamboaiplatform.model.entity.App;
import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import com.hachimi.mamboaiplatform.model.vo.AppVO;
import com.hachimi.mamboaiplatform.model.vo.UserPublicVO;
import com.hachimi.mamboaiplatform.service.AppService;
import com.hachimi.mamboaiplatform.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hachimi.mamboaiplatform.constant.AppConstant.*;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserPublicVO userPublicVO = userService.getUserPublicVO(user);
            appVO.setUser(userPublicVO);
        }
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        Boolean isVipOnly = appQueryRequest.getIsVipOnly();
        // 使用正确的 MyBatis-Flex QueryWrapper 语法
        // MyBatis-Flex 会自动忽略 null 值，无需手动判断
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .eq("isVipOnly", isVipOnly)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserPublicVO> UserPublicVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserPublicVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserPublicVO userPublicVO = UserPublicVOMap.get(app.getUserId());
            appVO.setUser(userPublicVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4.检验应用是否为vip专属，并且检验用户是否为vip
        if (app.getIsVipOnly() && userService.isVip(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "该应用为 VIP 专属应用，请先开通 VIP");
        }
        // 5. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 6. 调用 AI 生成代码
        return aiCodeGeneratorFacade.generateCodeAndSaveStream(message, codeGenTypeEnum, appId);
    }

    /**
     * 应用部署
     * @param appId
     * @param loginUser
     * @return
     */
    @Override
    public String deployApp(Long appId, User loginUser){
        // 1.参数校验
        if( appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        }
        if( loginUser == null || loginUser.getId() == null || loginUser.getId() <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录或登录信息不完整");
        }
        // 2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3.验证应用部署权限，只有本人才能部署应用
        if( !app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4.验证应用是否为vip专属，并且检验用户是否为vip
        if ( app.getIsVipOnly() && !userService.isVip(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "该应用为 VIP 专属应用，请先开通 VIP");
        }
        // 5.检查是否有deploykey(6位大小写字母+数字),没有则生成
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 6.获取代码生成类型，构建源目录路径
        String codeGenTypeStr = app.getCodeGenType();
        String sourceDirName = codeGenTypeStr + "_" + appId;
        String sourceDirPath = CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName; // 注意分割
        // 7.检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用代码生成目录不存在，请先生成代码");
        }
        // 8.复制文件到部署目录
        String deployDirPath = CODE_DEPLOY_ROOT_DIR + File.separator + deployKey ;
        FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        // 9.更新应用的deploykey和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setUpdateTime(LocalDateTime.now());
        // 调用保存
        this.updateById(updateApp);
        // 10.返回访问的url
        // 注意这里一定要带上/ 不然无法触发重定向
        return String.format("%s/%s/", CODE_DEPLOY_HOST, deployKey);
    }




}

