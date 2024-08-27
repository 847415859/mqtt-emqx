package com.qiankun.matt.broker.handler.v3;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.qiankun.matt.broker.context.ApplicationContext;
import com.qiankun.matt.broker.handler.MQTTConnectHandler;
import com.qiankun.matt.broker.session.ClientInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.netty.handler.codec.mqtt.MqttMessageType.SUBACK;
import static io.netty.handler.codec.mqtt.MqttMessageType.UNSUBACK;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_LEAST_ONCE;

/**
 * @Description:
 * @Date : 2024/08/27 11:52
 * @Auther : tiankun
 */
@Slf4j
public class MQTTV3ConnectHandler extends MQTTConnectHandler {

    public static final String NAME = "MQTTV3ConnectHandler";


    @Override
    protected void processDisconnect(ChannelHandlerContext ctx) {
        ApplicationContext.getContext().remove(ctx.channel());
    }

    @Override
    protected void processConnect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        log.info("connect channelId[{}]",ctx.channel().id().toString());
        ApplicationContext.getContext().addClientInfo(ctx.channel());
    }

    @Override
    protected void processPublish(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        log.info("publish channelId[{}]",ctx.channel().id().toString());
        String topicName = msg.variableHeader().topicName();
        ApplicationContext.getTopicManage().topicRouter(topicName).forEach(channelId -> {
            ClientInfo clientInfo = ApplicationContext.getContext().getClientInfo(channelId);
            if (ObjectUtil.isNotNull(clientInfo)) {
                log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", clientInfo.getClientId(), topicName, msg.fixedHeader().qosLevel().value());
                Channel channel = clientInfo.getChannel();
                if (channel != null) {
                    channel.writeAndFlush(msg);
                }
            }
        });
    }



    @Override
    protected void processSubscribe(ChannelHandlerContext ctx, MqttSubscribeMessage msg) {
        log.info("subscribe channelId[{}]",ctx.channel().id().toString());
        List<Integer> grantedQoSList = Lists.newArrayList();
        for (MqttTopicSubscription subscription : msg.payload().topicSubscriptions()) {
            String topic = subscription.topicName();
            MqttQoS qoS = subscription.qualityOfService();
            ApplicationContext.getTopicManage().subscribe(ctx.channel().id().toString(),topic);
            grantedQoSList.add(qoS.value());
        }
        ctx.writeAndFlush(createSubAckMessage(msg.variableHeader().messageId(), grantedQoSList));
    }


    private static MqttSubAckMessage createSubAckMessage(Integer msgId, List<Integer> grantedQoSList) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(SUBACK, false, AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(msgId);
        MqttSubAckPayload mqttSubAckPayload = new MqttSubAckPayload(grantedQoSList);
        return new MqttSubAckMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubAckPayload);
    }

    @Override
    protected void processUnsubscribe(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) {
        log.info("unsubscribe channelId[{}]",ctx.channel().id().toString());
        for (String topic : msg.payload().topics()) {
            ApplicationContext.getTopicManage().unsubscribe(ctx.channel().id().toString(),topic);
        }
        ctx.writeAndFlush(createUnSubAckMessage(msg.variableHeader().messageId()));
    }
    private MqttMessage createUnSubAckMessage(int msgId) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(UNSUBACK, false, AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(msgId);
        return new MqttMessage(mqttFixedHeader, mqttMessageIdVariableHeader);
    }
}
