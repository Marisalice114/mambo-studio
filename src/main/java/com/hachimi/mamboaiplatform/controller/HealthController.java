package com.hachimi.mamboaiplatform.controller;

import com.hachimi.mamboaiplatform.common.BaseResponse;
import com.hachimi.mamboaiplatform.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public BaseResponse<String> healthCheck() {

        return ResultUtils.success("OK");
    }
}
