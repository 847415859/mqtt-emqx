package com.qiankun.matt.broker.handler;

import com.qiankun.matt.broker.handler.v3.MQTTV3ConnectHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.*;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_MALFORMED_PACKET;
import static io.netty.handler.codec.mqtt.MqttMessageType.CONNECT;

/**
 * @Description:
 * @Date : 2024/08/27 11:45
 * @Auther : tiankun
 */
@Slf4j
public class MQTTPreludeHandler extends ChannelDuplexHandler {
    public static final String NAME = "MQTTPreludeHandler";

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        assert msg instanceof MqttMessage;
        // stop reading next message and resume reading once finish processing current one
        ctx.channel().config().setAutoRead(false);
        MqttMessage message = (MqttMessage) msg;
        // 检查消息是否解析成功
        if (!message.decoderResult().isSuccess()) {
            Throwable cause = message.decoderResult().cause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                closeChannelWithRandomDelay(ctx);
                return;
            }
            if (message.fixedHeader() != null && message.fixedHeader().messageType() != CONNECT) {
                closeChannelWithRandomDelay(ctx);
                return;
            }
            if (message.variableHeader() instanceof MqttConnectVariableHeader connVarHeader) {
                switch (connVarHeader.version()) {
                    case 3:
                    case 4:
                        if (cause instanceof TooLongFrameException) {
                            closeChannelWithRandomDelay(ctx);
                        } else if (cause instanceof MqttIdentifierRejectedException) {
                            closeChannelWithRandomDelay(ctx,
                                    MqttMessageBuilders.connAck()
                                            .returnCode(CONNECTION_REFUSED_IDENTIFIER_REJECTED)
                                            .build());
                        } else {
                            closeChannelWithRandomDelay(ctx);
                        }
                        return;
                    case 5:
                    default:
                        if (cause instanceof TooLongFrameException) {
                            MqttProperties.StringProperty stringProperty = new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(),
                                    cause.getMessage());
                            MqttProperties mqttProperties = new MqttProperties();
                            mqttProperties.add(stringProperty);
                            closeChannelWithRandomDelay(ctx,
                                    MqttMessageBuilders.connAck()
                                            .properties(mqttProperties)
                                            .returnCode(CONNECTION_REFUSED_PACKET_TOO_LARGE)
                                            .build());
                        } else if (cause instanceof MqttIdentifierRejectedException) {
                            // decode mqtt connect packet error
                            MqttProperties.StringProperty stringProperty = new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(),
                                    cause.getMessage());
                            MqttProperties mqttProperties = new MqttProperties();
                            mqttProperties.add(stringProperty);
                            closeChannelWithRandomDelay(ctx,
                                    MqttMessageBuilders.connAck()
                                            .properties(mqttProperties)
                                            .returnCode(CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID)
                                            .build());
                        } else {
                            // according to [MQTT-4.13.1-1]
                            MqttProperties.StringProperty stringProperty = new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(),
                                    cause.getMessage());
                            MqttProperties mqttProperties = new MqttProperties();
                            mqttProperties.add(stringProperty);
                            closeChannelWithRandomDelay(ctx,
                                    MqttMessageBuilders.connAck()
                                            .properties(mqttProperties)
                                            .returnCode(CONNECTION_REFUSED_MALFORMED_PACKET)
                                            .build());
                        }
                        return;
                }
            } else {
                closeChannelWithRandomDelay(ctx);
                return;
            }
        } else if (!(message instanceof MqttConnectMessage)) {
            // according to [MQTT-3.1.0-1]
            closeChannelWithRandomDelay(ctx);
            // log.warn("First packet must be mqtt connect message: remote={}", remoteAddr);
            return;
        }

        MqttConnectMessage connectMessage = (MqttConnectMessage) message;
        switch (connectMessage.variableHeader().version()) {
            case 3:
            case 4:
                ctx.pipeline().addAfter(ctx.executor(),
                        MQTTPreludeHandler.NAME, MQTTV3ConnectHandler.NAME, new MQTTV3ConnectHandler());
                ctx.channel().config().setAutoRead(true);
                // delegate to MQTT 3 handler
                ctx.fireChannelRead(connectMessage);
                ctx.pipeline().remove(this);
                break;
            case 5:
                ctx.pipeline().addAfter(ctx.executor(),
                        MQTTPreludeHandler.NAME, MQTTV3ConnectHandler.NAME, new MQTTV3ConnectHandler());
                // delegate to MQTT 5 handler
                ctx.fireChannelRead(connectMessage);
                ctx.pipeline().remove(this);
                break;
            default:
                log.warn("Unsupported protocol version: {}", connectMessage.variableHeader().version());
        }
    }

    private void closeChannelWithRandomDelay(ChannelHandlerContext ctx) {
        closeChannelWithRandomDelay(ctx,null);
    }

    private void closeChannelWithRandomDelay(ChannelHandlerContext ctx,MqttMessage msg) {
        ctx.executor().schedule(() -> {
            if (!ctx.channel().isActive()) {
                return;
            }
            if (msg != null) {
                ctx.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.channel().close();
            }
        }, ThreadLocalRandom.current().nextInt(5000), TimeUnit.MILLISECONDS);
    }
}
