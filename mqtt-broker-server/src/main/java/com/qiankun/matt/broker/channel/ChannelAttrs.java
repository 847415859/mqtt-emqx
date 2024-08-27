package com.qiankun.matt.broker.channel;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * @Description:
 * @Date : 2024/08/27 11:37
 * @Auther : tiankun
 */
public class ChannelAttrs {
    public static String getClientId(Channel channel) {
        return channel.attr(getClientId()).get();
    }

    public static void setClientId(Channel channel, String clientId) {
        channel.attr(getClientId()).set(clientId);
    }
    private static AttributeKey<String> getClientId() {
        return AttributeKey.valueOf("CLIENT_ID");
    }


    public static String getName(Channel channel) {
        return channel.attr(getName()).get();
    }

    public static void setName(Channel channel, String name) {
        channel.attr(getName()).set(name);
    }
    private static AttributeKey<String> getName() {
        return AttributeKey.valueOf("NAME");
    }

    public static String getPassword(Channel channel) {
        return channel.attr(getPassword()).get();
    }

    public static void setPassword(Channel channel, String password) {
        channel.attr(getPassword()).set(password);
    }
    private static AttributeKey<String> getPassword() {
        return AttributeKey.valueOf("PASSWORD");
    }
}
