package com.netdisk.exception;

/**
 * 用户已存在异常
 */
public class UserAlreadyExistException extends BaseException {
    public UserAlreadyExistException() {
    }

    public UserAlreadyExistException(String msg) {
        super(msg);
    }
}
