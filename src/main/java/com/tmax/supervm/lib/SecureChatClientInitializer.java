package com.tmax.supervm.lib;

import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.stomp.StompSubframeAggregator;
import io.netty.handler.codec.stomp.StompSubframeDecoder;
import io.netty.handler.codec.stomp.StompSubframeEncoder;
import io.netty.handler.ssl.SslContext;

public class SecureChatClientInitializer extends ChannelInitializer {

    private final SslContext sslctx;
    private JsonObject jsonObject;
    private StringBuilder stringBuilder;

    public SecureChatClientInitializer(SslContext sslCtx, JsonObject jsonObject,StringBuilder stringBuilder){
        this.sslctx = sslCtx;
        this.jsonObject = jsonObject;
        this.stringBuilder = stringBuilder;
    }
    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(sslctx.newHandler(channel.alloc(), VDSMClient.HOST, VDSMClient.PORT));
        pipeline.addLast(new StompSubframeDecoder());
        pipeline.addLast(new StompSubframeEncoder());
        pipeline.addLast(new StompSubframeAggregator(1048576));
        pipeline.addLast(new StompClientHandler(jsonObject, stringBuilder));
    }
}
