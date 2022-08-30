package com.tmax.supervm.lib;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.stomp.StompSubframeAggregator;
import io.netty.handler.codec.stomp.StompSubframeDecoder;
import io.netty.handler.codec.stomp.StompSubframeEncoder;

public class NoSSLVDSMClient {
    static final String HOST = System.getProperty("host", "192.168.9.46");
    static final int PORT = Integer.parseInt(System.getProperty("port", "54321"));
    static final String REQ_TOPIC = System.getProperty("topic", "jms.topic.vdsm_requests");
    static final String RES_TOPIC = System.getProperty("topic", "jms.topic.vdsm_responses");

    private static NoSSLVDSMClient instance;

    private NoSSLVDSMClient(){

    }

    public static NoSSLVDSMClient getInstance(){
        if(instance ==null){
            instance = new NoSSLVDSMClient();
        }
        return instance;
    }

    public JsonElement start(JsonObject jsonObject,StringBuilder stringBuilder) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();

            b.group(bossGroup).channel(NioSocketChannel.class);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("decoder", new StompSubframeDecoder());
                    pipeline.addLast("encoder", new StompSubframeEncoder());
                    pipeline.addLast("aggregator", new StompSubframeAggregator(1048576));
                    pipeline.addLast("handler", new StompClientHandler(jsonObject,stringBuilder));
                }
            });
            ChannelFuture f = b.connect(HOST, PORT).sync();

            f.channel().closeFuture().sync();
        } finally {
            JsonElement res = new JsonParser().parse(stringBuilder.toString());
            bossGroup.shutdownGracefully();
            return res;
        }
    }
}
