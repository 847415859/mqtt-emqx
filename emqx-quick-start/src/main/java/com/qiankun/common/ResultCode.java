package com.qiankun.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
  * @ClassName
  * @Description
  * @Author wzq
  * @Date 2024/7/10 16:38
  * @Version 1.0
  */
@Getter
@AllArgsConstructor
public enum ResultCode
{

    /**
     *
     */
    SUCCESS(200, "成功"),
    FAIL(101, "失败"),
    SYSTEM_BUSY(1000, "系统繁忙"),
    UNKNOWN(9999999, "未知错误");

    /**
     * 状态码
     */
    private final Integer code;
    /**
     * 状态描述
     */
    private final String msg;


    public static ResultCode of(Integer code) {
        return Arrays.stream(ResultCode.values()).filter(values -> values.code.equals(code)).findFirst().orElse(UNKNOWN);
    }

    public static ResultCode ofNull(Integer code) {
        return Arrays.stream(ResultCode.values()).filter(values -> values.code.equals(code)).findFirst().orElse(null);
    }

}
