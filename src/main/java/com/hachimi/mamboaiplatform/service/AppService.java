package com.hachimi.mamboaiplatform.service;

import com.hachimi.mamboaiplatform.model.dto.app.AppAddRequest;
import com.hachimi.mamboaiplatform.model.dto.app.AppQueryRequest;
import com.hachimi.mamboaiplatform.model.entity.App;
import com.hachimi.mamboaiplatform.model.entity.User;
import com.hachimi.mamboaiplatform.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/Marisalice114">Marisalice114</a>
 */
public interface AppService extends IService<App> {

    AppVO getAppVO(App app);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    Long createApp(AppAddRequest appAddRequest, User loginUser);

    String deployApp(Long appId, User loginUser);

    void generateAppScreenshotAsync(Long appId, String appDeployUrl);
}
