package com.qiankun.matt.broker.session;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @Description:
 * @Date : 2024/08/27 11:35
 * @Auther : tiankun
 */
@Data
@Accessors(chain = true)
public class ClientInfo {
    // channel 通道
    private Channel channel;
    // 通道id
    private String channelId;
    // 客户端ip
    private String ip;
    // 上线时间
    private LocalDateTime onlineTime;
    // 客户端id
    private String clientId;
    // 密码
    private String password;
    // 用户名
    private String name;
}
