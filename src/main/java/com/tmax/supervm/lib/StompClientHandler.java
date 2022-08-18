package com.tmax.supervm.lib;

import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompFrame;
import io.netty.handler.codec.stomp.StompHeaders;
import io.netty.util.CharsetUtil;

public class StompClientHandler extends SimpleChannelInboundHandler<StompFrame> {

    private JsonObject jsonobj;
    private StringBuilder result;
    public StompClientHandler(JsonObject jsonobj){
        this.jsonobj = jsonobj;
    }

    public StompClientHandler(JsonObject jsonobj, StringBuilder result){
        this.jsonobj = jsonobj;
        this.result = result;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        StompFrame connFrame = new DefaultStompFrame(StompCommand.CONNECT);
        connFrame.headers().set(StompHeaders.ACCEPT_VERSION, "1.2");
        connFrame.headers().set(StompHeaders.HOST, VDSMClient.HOST);
        System.out.println("Client - Channel Ative!");
        ctx.writeAndFlush(connFrame);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompFrame frame) throws Exception {
        String subscrReceiptId = "001";
        String disconReceiptId = "002";

        System.out.println("Client - ChannelRead Ocurred!");
        switch (frame.command()) {
            case CONNECTED:
                StompFrame subscribeFrame = new DefaultStompFrame(StompCommand.SUBSCRIBE);
                subscribeFrame.headers().set(StompHeaders.DESTINATION, VDSMClient.RES_TOPIC);
                subscribeFrame.headers().set(StompHeaders.RECEIPT, subscrReceiptId);
                subscribeFrame.headers().set(StompHeaders.ID, "1");
                System.out.println("connected, sending subscribe frame: " + subscribeFrame);
                ctx.writeAndFlush(subscribeFrame);

                Thread.sleep(500);

                StompFrame msgFrame = new DefaultStompFrame(StompCommand.SEND);
                msgFrame.headers().set(StompHeaders.DESTINATION, VDSMClient.REQ_TOPIC);
                msgFrame.headers().set("reply-to", VDSMClient.RES_TOPIC);

                String cont = jsonobj.toString();

                msgFrame.content().writeBytes(cont.getBytes());
                System.out.println("subscribed, sending message frame: " + msgFrame);
                ctx.writeAndFlush(msgFrame);
                break;
            case RECEIPT:
                String receiptHeader = frame.headers().getAsString(StompHeaders.RECEIPT_ID);
                if (receiptHeader.equals(subscrReceiptId)) {
                } else if (receiptHeader.equals(disconReceiptId)) {
                    System.out.println("disconnected");
                    ctx.close();
                }
            case MESSAGE:
                System.out.println("received frame: " + frame);
                result.append(frame.content().toString(CharsetUtil.UTF_8));
                StompFrame disconnFrame = new DefaultStompFrame(StompCommand.DISCONNECT);
                disconnFrame.headers().set(StompHeaders.RECEIPT, disconReceiptId);
                System.out.println("sending disconnect frame: " + disconnFrame);
                ctx.writeAndFlush(disconnFrame);
                break;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

