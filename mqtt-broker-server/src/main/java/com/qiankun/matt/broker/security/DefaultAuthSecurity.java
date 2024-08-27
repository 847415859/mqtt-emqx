package com.qiankun.matt.broker.security;

import lombok.Getter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Description:
 * @Date : 2024/08/27 11:34
 * @Auther : tiankun
 */
public class DefaultAuthSecurity implements IAuthSecurity{
    @Getter
    private final String prefix = "qiankun";

    // 用户名和密码的映射关系
    private final ConcurrentMap<String/* name */, AuthInfo> authInfoTable =
            new ConcurrentHashMap<>();

    @Override
    public void addAuthSecurity(AuthInfo authInfo) throws Exception {
        String password = encryptMD5(prefix + authInfo.getPassword());
        authInfo.setPassword(password);
        authInfoTable.put(authInfo.getName(),authInfo);
    }

    @Override
    public void removeAuthSecurity(String name) {
        authInfoTable.remove(name);
    }

    @Override
    public Boolean authSecurity(AuthInfo authInfo) throws Exception {
        AuthInfo dbAuthInfo = authInfoTable.computeIfPresent(authInfo.getName(), (k, v) -> v);
        if (dbAuthInfo != null ){
            String password = encryptMD5(prefix + authInfo.getPassword());
            return dbAuthInfo.getPassword().equals(password);
        }else {
            return false;
        }
    }


    private String encryptMD5(String data) throws Exception {

        return byte2hex(encryptMD5(data.getBytes("UTF-8")));

    }

    private byte[] encryptMD5(byte[] data) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(data);
            return bytes;
        } catch (GeneralSecurityException var3) {
            throw new IOException(var3.toString());
        }
    }

    private String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();

        for(int i = 0; i < bytes.length; ++i) {
            String hex = Integer.toHexString(bytes[i] & 255);
            if (hex.length() == 1) {
                sign.append("0");
            }

            sign.append(hex);
        }

        return sign.toString();
    }

}
