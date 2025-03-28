package com.netdisk.cloudserver.controller;

import com.netdisk.properties.DiskProperties;
import com.netdisk.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/test")
public class TestController {

    private DiskProperties diskProperties;

//    public TestController(DiskProperties diskProperties) {
//        this.diskProperties = diskProperties;
//    }

    @GetMapping("/hi")
    public Result hi() {
        log.info("nihao");
        return Result.success("你好,测试成功");
    }

    @GetMapping("/getStoragePath")
    public Result getStoragePath() {
        log.info("存储路径: {}", diskProperties.getStoragePath());
        return Result.success("测试成功");
    }


}
