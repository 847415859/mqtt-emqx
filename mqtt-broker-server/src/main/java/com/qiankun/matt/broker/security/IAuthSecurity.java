package com.qiankun.matt.broker.security;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Description: 认证接口
 * @Date : 2024/08/27 11:34
 * @Auther : tiankun
 */
public interface IAuthSecurity {

    void addAuthSecurity(AuthInfo authInfo) throws Exception;

    void removeAuthSecurity(String name);

    Boolean authSecurity(AuthInfo authInfo) throws Exception;

    @Data
    @Accessors(chain = true)
    class AuthInfo{
        private String name;

        private String password;
    }
}
