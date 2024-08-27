package com.qiankun.matt.broker.handler;

import com.qiankun.matt.broker.channel.ChannelAttrs;
import com.qiankun.matt.broker.context.ApplicationContext;
import com.qiankun.matt.broker.security.IAuthSecurity;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.mqtt.MqttMessageType.CONNACK;
import static io.netty.handler.codec.mqtt.MqttMessageType.PINGRESP;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * @Description:
 * @Date : 2024/08/27 11:43
 * @Auther : tiankun
 */
@Slf4j
public abstract class MQTTConnectHandler extends ChannelDuplexHandler {
    private volatile boolean connected;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connected = false;
        ApplicationContext.getContext().remove(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MqttMessage mqttMessage =  (MqttMessage) msg;
        if (mqttMessage.fixedHeader() == null) {
            processDisconnectCompose(ctx);
            return;
        }

        switch (mqttMessage.fixedHeader().messageType()) {
            // 连接
            case CONNECT:
                processConnectCompose(ctx, (MqttConnectMessage) msg);
                break;
            // 发布消息
            case PUBLISH:
                if (checkConnected(ctx)) {
                    processPublish(ctx, (MqttPublishMessage) msg);
                }
                break;
            // 订阅
            case SUBSCRIBE:
                if (checkConnected(ctx)) {
                    processSubscribe(ctx, (MqttSubscribeMessage) msg);
                }
                break;
            // 取消订阅
            case UNSUBSCRIBE:
                if (checkConnected(ctx)) {
                    processUnsubscribe(ctx, (MqttUnsubscribeMessage) msg);
                }
                break;
            // 心跳-PING/PONG
            case PINGREQ:
                if (checkConnected(ctx)) {
                    ctx.writeAndFlush(new MqttMessage(new MqttFixedHeader(PINGRESP, false, AT_MOST_ONCE, false, 0)));
                }
                break;
            // 断开连接
            case DISCONNECT:
                if (checkConnected(ctx)) {
                    processDisconnectCompose(ctx);
                }
                break;
            default:
                break;

        }

    }

    private Boolean processAuthSecurity(ChannelHandlerContext ctx, MqttConnectMessage msg) throws Exception {
        String username = msg.payload().userName();
        String password = "";
        if (msg.variableHeader().hasPassword()) {
            try {
                password = new String(msg.payload().passwordInBytes(), "utf-8");
            } catch (Exception e) {
                log.error("password convert error msg[{}]",e.getMessage(),e);
            }
        }
        String clientId = msg.payload().clientIdentifier();
        ChannelAttrs.setClientId(ctx.channel(),clientId);
        ChannelAttrs.setName(ctx.channel(),username);
        ChannelAttrs.setPassword(ctx.channel(),password);
        return ApplicationContext.getAuthSecurity().authSecurity(new IAuthSecurity.AuthInfo().setName(username).setPassword(password));
    }

    private MqttConnAckMessage createMqttConnAckMsg(MqttConnectReturnCode returnCode) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(CONNACK, false, AT_MOST_ONCE, false, 0);
        MqttConnAckVariableHeader mqttConnAckVariableHeader =
                new MqttConnAckVariableHeader(returnCode, true);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }



    private void processConnectCompose(ChannelHandlerContext ctx,MqttConnectMessage msg) throws Exception {
        if (processAuthSecurity(ctx,msg)){
            connected = true;
            processConnect(ctx,msg);
            ctx.writeAndFlush(createMqttConnAckMsg(MqttConnectReturnCode.CONNECTION_ACCEPTED));
        }else {
            connected = false;
            ctx.writeAndFlush(createMqttConnAckMsg(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD));
        }
    }

    protected void processDisconnectCompose(ChannelHandlerContext ctx){
        log.info("disconnect channelId[{}]",ctx.channel().id().toString());
        connected = false;
        try {
            processDisconnect(ctx);
        }catch (Exception e){
            log.error("disconnect error msg[{}]",e.getMessage(),e);
        }finally {
            ctx.channel().close();
            ctx.close();
        }
    }

    protected abstract void processDisconnect(ChannelHandlerContext ctx);

    protected abstract void processConnect(ChannelHandlerContext ctx,MqttConnectMessage msg);

    protected abstract void processPublish(ChannelHandlerContext ctx,MqttPublishMessage msg);

    protected abstract void processSubscribe(ChannelHandlerContext ctx,MqttSubscribeMessage msg);

    protected abstract void processUnsubscribe(ChannelHandlerContext ctx,MqttUnsubscribeMessage msg);

    protected  Boolean checkConnected(ChannelHandlerContext ctx){
        if (connected) {
            return true;
        } else {
            ctx.channel().close();
            ctx.close();
            return false;
        }
    }
}
