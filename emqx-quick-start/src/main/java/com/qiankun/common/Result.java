package com.qiankun.common;

import lombok.Data;

import java.util.Objects;

/**
  * @ClassName
  * @Description
  * @Author wzq
  * @Date 2024/7/10 16:37
  * @Version 1.0
  */
@Data
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 状态描述
     */
    private String message;

    /**
     * 数据
     */
    private T body;

    public Result() {
        this(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg());
    }

    public Result(Integer code, String message) {
        this(code, message, null);
    }

    public Result(Integer code, String message, T body) {
        this.code = code;
        this.message = message;
        this.body = body;
    }

    /**
      * 返回成功消息
      */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * 返回成功消息
     *
     * @param body 数据
     * @return 成功消息
     */
    public static <T> Result<T> ok(T body) {
        return ok(ResultCode.SUCCESS.getMsg(), body);
    }

    /**
     * 返回成功消息
     *
     * @param message 状态描述
     * @param body    数据
     * @return 成功消息
     */
    public static <T> Result<T> ok(String message, T body) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, body);
    }

    /**
     * 返回失败消息
     *
     * @param message 状态描述
     * @return 失败消息
     */
    public static <T> Result<T> fail(String message) {
        return fail(ResultCode.FAIL.getCode(), message, null);
    }

    /**
     * 返回失败消息
     *
     * @param message 状态描述
     * @param body    数据
     * @return 失败消息
     */
    public static <T> Result<T> fail(String message, T body) {
        return fail(ResultCode.FAIL.getCode(), message, body);
    }

    /**
     * 返回失败消息
     *
     * @param code    状态码
     * @param message 状态描述
     * @return 失败消息
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return fail(code, message, null);
    }

    /**
     * 返回失败消息
     *
     * @param resultCode 错误码枚举
     * @return
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMsg());
    }

    /**
     * 返回失败消息
     *
     * @param resultCode 错误码枚举
     * @param body           数据
     * @return
     */
    public static <T> Result<T> fail(ResultCode resultCode, T body) {
        return fail(resultCode.getCode(), resultCode.getMsg(), body);
    }

    /**
     * 返回失败消息
     *
     * @param code    状态码
     * @param message 状态描述
     * @param body    数据
     * @return
     */
    public static <T> Result<T> fail(Integer code, String message, T body) {
        return new Result<>(code, message, body);
    }

    /**
     * 是否成功
     *
     * @return 结果
     */
    public boolean isSuccess() {
        return Objects.equals(this.code, ResultCode.SUCCESS.getCode());
    }
}
