package com.hachimi.mamboaiplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hachimi.mamboaiplatform.ai.AiCodeGenTypeRoutingService;
import com.hachimi.mamboaiplatform.constant.AppConstant;
import com.hachimi.mamboaiplatform.core.AiCodeGeneratorFacade;
import com.hachimi.mamboaiplatform.core.builder.VueProjectBuilder;
import com.hachimi.mamboaiplatform.core.handler.StreamHandlerExecutor;
import com.hachimi.mamboaiplatform.exception.BusinessException;
import com.hachimi.mamboaiplatform.exception.ErrorCode;
import com.hachimi.mamboaiplatform.exception.ThrowUtils;
import com.hachimi.mamboaiplatform.mapper.AppMapper;
import com.hachimi.mamboaiplatform.model.dto.app.AppAddRequest;
import com.hachimi.mamboaiplatform.model.dto.app.AppQueryRequest;
import com.hachimi.mamboaiplatform.model.entity.App;
import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.model.enums.ChatHistoryMessageTypeEnum;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;
import com.hachimi.mamboaiplatform.model.vo.AppVO;
import com.hachimi.mamboaiplatform.model.vo.UserPublicVO;
import com.hachimi.mamboaiplatform.service.AppService;
import com.hachimi.mamboaiplatform.service.ChatHistoryService;
import com.hachimi.mamboaiplatform.service.ScreenshotService;
import com.hachimi.mamboaiplatform.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
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
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

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
        if (app.getIsVipOnly() && !userService.isVip(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "该应用为 VIP 专属应用，请先开通 VIP");
        }
        // 5. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }

        // 6. 调用 AI 前，先将用户消息保存到数据库中
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 7. 调用 AI 生成代码，并返回结果流
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateCodeAndSaveStream(message, codeGenTypeEnum, appId);
        // 8.收集响应内容并在完成过后记录到对话历史中
        return streamHandlerExecutor.doExecutor(stringFlux, codeGenTypeEnum,chatHistoryService, appId, loginUser);

    }


    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用 AI 智能选择代码生成类型
        CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.getCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
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
        // vue处理
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if(codeGenType == CodeGenTypeEnum.VUE_PROJECT) {
            boolean result = vueProjectBuilder.buildVueProject(sourceDirPath);
            ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "Vue项目构建失败，请检查代码生成目录是否正确");
            // 检查生成的目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            if (!distDir.exists() || !distDir.isDirectory()) {
                ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            }
            sourceDir = distDir; // 将源目录指向 dist 目录
        }
        // 8.复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败：" + e.getMessage());
        }
        // 9.更新应用的deploykey和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setUpdateTime(LocalDateTime.now());
        // 调用保存
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf( !updateResult, ErrorCode.SYSTEM_ERROR, "应用部署信息更新失败，请稍后重试");
        // 10.返回访问的url
        // 注意这里一定要带上/ 不然无法触发重定向
        String appDeployUrl =  String.format("%s/%s/", CODE_DEPLOY_HOST, deployKey);
        // 11.异步生成封面
        generateAppScreenshotAsync(appId,appDeployUrl);
        return appDeployUrl;
    }

    /**
     * 异步生成应用封面截图
     * @param appId
     * @param appDeployUrl
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appDeployUrl) {
        // 异步生成应用封面截图
        Thread.startVirtualThread(() -> {
            try {
                // 生成应用封面截图
                String screenshotUrl = screenshotService.generateAndUploadScreenshot(appDeployUrl);
                if (StrUtil.isNotBlank(screenshotUrl)) {
                    // 更新应用的封面
                    App updateApp = new App();
                    updateApp.setId(appId);
                    updateApp.setCover(screenshotUrl);
                    this.updateById(updateApp);
                    log.info("应用 {} 的封面截图已生成并更新: {}", appId, screenshotUrl);
                } else {
                    log.warn("应用 {} 的封面截图生成失败", appId);
                }
            } catch (Exception e) {
                log.error("生成应用 {} 封面截图异常: {}", appId, e.getMessage(), e);
            }
        });
    }

    /**
     * 删除应用时，关联删除对话历史（此处不需要事务）
     * @param id
     * @return
     */
    @Override
    public boolean removeById(Serializable id) {
        //1.空值判断
        ThrowUtils.throwIf(id == null || !(id instanceof Long) || (Long) id <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        long appId = Long.parseLong(id.toString());
        if (appId<=0){
            return false;
        }
        //2.删除关联的对话历史
        // 这里不需要事务，因为删除对话历史失败不会影响应用删除
        try{
            chatHistoryService.removeById(appId);
        }catch(Exception e){
            log.error("删除应用关联的对话历史失败: {}", e.getMessage());
        }
        //3.删除应用
        return super.removeById(id);
    }


}
