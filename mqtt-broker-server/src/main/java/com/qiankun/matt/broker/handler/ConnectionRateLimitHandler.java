package com.qiankun.matt.broker.handler;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 流控
 * @Date : 2024/08/27 11:51
 * @Auther : tiankun
 */
@Slf4j
@ChannelHandler.Sharable    // 共享
public class ConnectionRateLimitHandler extends ChannelDuplexHandler {
    private final RateLimiter rateLimiter;

    public ConnectionRateLimitHandler(int rate) {
        rateLimiter = RateLimiter.create(rate);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (rateLimiter.tryAcquire()) {
            ctx.fireChannelActive();
        } else {
            log.warn("Connection dropped due to exceed limit");
            // close the connection randomly
            ctx.channel().config().setAutoRead(false);
            ctx.executor().schedule(() -> {
                if (ctx.channel().isActive()) {
                    ctx.close();
                }
            }, ThreadLocalRandom.current().nextLong(3000, 5000), TimeUnit.MILLISECONDS);
        }
    }
}
