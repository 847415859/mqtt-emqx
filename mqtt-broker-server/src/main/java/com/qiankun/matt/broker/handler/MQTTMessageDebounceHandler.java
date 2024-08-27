package com.qiankun.matt.broker.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @Description:
 * @Date : 2024/08/27 11:50
 * @Auther : tiankun
 */
@Slf4j
public class MQTTMessageDebounceHandler extends ChannelDuplexHandler {
    public static final String NAME = "MQTTMessageDebounceHandler";

    private final Queue<MqttMessage> buffer = new LinkedList<>();
    private boolean readOne = false;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        MqttMessage msg;
        while ((msg = buffer.poll()) != null) {
            ReferenceCountUtil.release(msg);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void read(ChannelHandlerContext ctx) {
        if (ctx.channel().config().isAutoRead()) {
            MqttMessage msg;
            while ((msg = buffer.poll()) != null) {
                ctx.fireChannelRead(msg);
            }
            ctx.read();
        } else {
            MqttMessage msg = buffer.poll();
            if (msg != null) {
                ctx.fireChannelRead(msg);
            } else {
                readOne = true;
                ctx.read();
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        assert msg instanceof MqttMessage;
        if (ctx.channel().config().isAutoRead()) {
            ctx.fireChannelRead(msg);
        } else {
            buffer.offer((MqttMessage) msg);
            if (readOne) {
                MqttMessage mqttMsg = buffer.poll();
                ctx.fireChannelRead(mqttMsg);
                readOne = false;
            }
        }
    }
}
