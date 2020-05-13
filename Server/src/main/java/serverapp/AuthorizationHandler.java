package serverapp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import serverapp.common.MyMessageAuth;

import java.util.Arrays;

public class AuthorizationHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        MyMessageAuth mess = new MyMessageAuth();
        String str = mess.formReceiveMsg(in);
        System.out.println(str);
        String[] token = str.split(" ");
        System.out.println(Arrays.toString(token));
        System.out.println(token[3]);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
