package com.qiankun.matt.broker.broker;

import com.qiankun.matt.broker.handler.ConnectionRateLimitHandler;
import com.qiankun.matt.broker.handler.MQTTMessageDebounceHandler;
import com.qiankun.matt.broker.handler.MQTTPreludeHandler;
import com.qiankun.matt.broker.util.NettyUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Date : 2024/08/27 11:40
 * @Auther : tiankun
 */
@Slf4j
public abstract class AbstractMQTTBroker implements IMQTTBroker{
    private final AbstractMQTTBrokerBuilder builder;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private ChannelFuture tcpChannelF;

    private final ConnectionRateLimitHandler connRateLimitHandler;


    protected AbstractMQTTBroker(AbstractMQTTBrokerBuilder builder) {
        this.builder = builder;
        connRateLimitHandler = new ConnectionRateLimitHandler(builder.connectRateLimit);
        this.bossGroup =  NettyUtil.createEventLoopGroup(this.builder.mqttBossThreads,
                newThreadFactory("mqtt-boss-thread",false, Thread.NORM_PRIORITY));
        this.workerGroup = NettyUtil.createEventLoopGroup(builder.mqttWorkerThreads,
                newThreadFactory("mqtt-worker-thread",false, Thread.NORM_PRIORITY));
    }

    protected void beforeBrokerStart() {

    }

    protected void afterBrokerStop() {

    }

    @Override
    public final void start(){
        try {
            log.info("Starting MQTT broker");
            beforeBrokerStart();
            log.debug("Starting server channel");

            tcpChannelF = this.bindTCPChannel();
            Channel channel = tcpChannelF.sync().channel();
            log.debug("Accepting mqtt connection over tcp channel at {}", channel.localAddress());


            log.info("MQTT broker started");
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final void shutdown() {
        log.info("Shutting down MQTT broker");
        if (tcpChannelF != null) {
            tcpChannelF.channel().close().syncUninterruptibly();
            log.debug("Stopped accepting mqtt connection over tcp channel");
        }

        bossGroup.shutdownGracefully().syncUninterruptibly();
        log.debug("Boss group shutdown");
        workerGroup.shutdownGracefully().syncUninterruptibly();
        log.debug("Worker group shutdown");
        afterBrokerStop();
        log.info("MQTT broker shutdown");
    }

    private ChannelFuture bindTCPChannel() {

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NettyUtil.determineServerSocketChannelClass(bossGroup))
                .childHandler(  new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {

                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("connRateLimiter", connRateLimitHandler);
                        pipeline.addLast("trafficShaper",
                                new ChannelTrafficShapingHandler(builder.writeLimit, builder.readLimit));
                        pipeline.addLast(MqttEncoder.class.getName(), MqttEncoder.INSTANCE);
                        pipeline.addLast(MqttDecoder.class.getName(), new MqttDecoder(builder.maxBytesInMessage));
                        pipeline.addLast(MQTTMessageDebounceHandler.NAME, new MQTTMessageDebounceHandler());
                        pipeline.addLast(MQTTPreludeHandler.NAME, new MQTTPreludeHandler());
                    }
                });

        // Bind and start to accept incoming connections.
        return b.bind(builder.port);

    }


    public static ThreadFactory newThreadFactory(String name, boolean daemon, int priority) {
        return new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                int s = seq.getAndIncrement();
                t.setName(s > 0 ? name + "-" + s : name);
                t.setDaemon(daemon);
                t.setPriority(priority);
                return t;
            }
        };
    }
}
