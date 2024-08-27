package com.qiankun.matt.broker.session;

import cn.hutool.core.util.StrUtil;
import com.qiankun.matt.broker.channel.ChannelAttrs;
import com.qiankun.matt.broker.lock.IKeyLock;
import com.qiankun.matt.broker.lock.ReentrantKeyLock;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Description:
 * @Date : 2024/08/27 11:36
 * @Auther : tiankun
 */
@Data
@Accessors(chain = true)
public class MQTTSessionContext {
    private final ConcurrentMap<String/* channelId */, ClientInfo> clientInfoTable =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String/* clientId */, String/*channelId*/> clientChannelIdTable =
            new ConcurrentHashMap<>();

    private final IKeyLock keyLock = new ReentrantKeyLock();

    public void addClientInfo(Channel channel){
        String clientId = ChannelAttrs.getClientId(channel);
        try {
            if (keyLock.tryLock(clientId)){
                remove(clientId);
                clientChannelIdTable.putIfAbsent(clientId,channel.id().toString());
                InetSocketAddress ipSocket = (InetSocketAddress)channel.remoteAddress();
                clientInfoTable.putIfAbsent(channel.id().toString(), new ClientInfo()
                        .setChannel(channel)
                        .setClientId(clientId)
                        .setIp(ipSocket.getAddress().getHostAddress())
                        .setName(ChannelAttrs.getName(channel))
                        .setPassword(ChannelAttrs.getPassword(channel))
                        .setOnlineTime(LocalDateTime.now()));
            }
        }catch (Exception e){

        }finally {
            keyLock.unlock(clientId);
        }
    }

    public ClientInfo getClientInfo(String channelId){
        return clientInfoTable.get(channelId);
    }

    public void remove(Channel channel) {
        ClientInfo clientInfo = clientInfoTable.get(channel.id().toString());
        if (clientInfo != null) {
            clientInfoTable.remove(channel.id().toString());
            remove(clientInfo.getClientId());
        }
    }

    private void remove(String clientId) {
        try {
            String channelId = clientChannelIdTable.get(clientId);
            if (StrUtil.isNotBlank(channelId)) {
                removeClientInfo(channelId);
                clientChannelIdTable.remove(clientId);
            }
        } catch (Exception e) {

        }

    }

    private void removeClientInfo(String channelId) {
        ClientInfo clientInfo = clientInfoTable.get(channelId);
        if (clientInfo != null) {
            clientInfoTable.remove(channelId);
        }
    }
}
