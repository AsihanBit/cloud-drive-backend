package com.netdisk.cloudserver.controller;

import com.netdisk.properties.DiskProperties;
import com.netdisk.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {

    private DiskProperties diskProperties;

//    public TestController(DiskProperties diskProperties) {
//        this.diskProperties = diskProperties;
//    }

    @GetMapping("/test")
    public Result test1() {
        log.info("nihao");
        log.info("存储路径: {}", diskProperties.getStoragePath());
        return Result.success("测试成功");
    }
}
