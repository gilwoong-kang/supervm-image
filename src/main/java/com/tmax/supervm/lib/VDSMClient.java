package com.tmax.supervm.lib;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.UUID;

public class VDSMClient {
    private static VDSMClient instance;
    static final String HOST = System.getProperty("host", "192.168.9.125");
    static final int PORT = Integer.parseInt(System.getProperty("port", "54321"));
    static final String REQ_TOPIC = System.getProperty("topic", "jms.topic.vdsm_requests");
    static final String RES_TOPIC = System.getProperty("topic", "jms.topic.vdsm_responses");
    private static final String KEYSTORE_PATH = "/Users/gilwoongkang/personalLibrary/tmax/SuperVM_Image_Service/SuperVMImageService/build/libs/vdsmkeystore.p12";
    private static final String KEYSTORE_PASS = "tmax@23";
    private final String JSONRPC_VERSION = "2.0";

    private static SslContext sslctx;

    private final static boolean SSL = false;

    private VDSMClient(){
    }

    public static VDSMClient getInstance() throws Exception{
        if(instance==null){
            instance = new VDSMClient();
            KeyStore ks = KeyStore.getInstance("pkcs12");
            ks.load(new FileInputStream(new File(KEYSTORE_PATH)), KEYSTORE_PASS.toCharArray());
            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, KEYSTORE_PASS.toCharArray());
            sslctx = SslContextBuilder.forClient().keyManager(kmf).build();
        }
        return instance;
    }

    /**
        @param :  method type, params with JSONOBJECT. ex) {"method":"Host.getStats","params":{"your_key":"your_value"}}
        @return : jsonrpc content return. JsonElement type.
     */
    public JsonElement start(JsonObject jsonobj) throws Exception {

        StringBuilder result = new StringBuilder();
        jsonobj.addProperty("id", UUID.randomUUID().toString());
        jsonobj.addProperty("jsonrpc",JSONRPC_VERSION);

        if(!SSL){
            return NoSSLVDSMClient.getInstance().start(jsonobj,result);
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup();


        try {
            Bootstrap b = new Bootstrap();

            b.group(bossGroup).channel(NioSocketChannel.class);
            b.handler(new SecureChatClientInitializer(sslctx,jsonobj,result));

            ChannelFuture cf = b.connect(HOST,PORT).sync();

            cf.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            JsonElement res = new JsonParser().parse(result.toString());
            return res;
        }
    }
}
