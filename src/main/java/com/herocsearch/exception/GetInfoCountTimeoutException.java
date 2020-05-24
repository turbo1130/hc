package com.herocsearch.exception;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA
 * User: heroC
 * Date: 2020/5/23
 * Time: 15:44
 * Description: 获取es中数据总条数超时的异常
 * Version: V1.0
 */
public class GetInfoCountTimeoutException extends Exception implements Serializable {
    private static final long serialVersionUID = 1130002L;
    @Override
    public String toString() {
        return "GetInfoCountTimeoutException{ timeout = 20000 }：获取信息总条数超时30s...";
    }
}
