package com.netdisk.cloudserver.handler;

import com.netdisk.exception.BaseException;
import com.netdisk.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 捕获业务异常
     *
     * @param ex
     * @return
     */
//    @ExceptionHandler(BaseException.class)
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("业务异常信息: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
}
