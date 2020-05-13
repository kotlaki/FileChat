package serverapp;

import common.MyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


import java.util.Arrays;

public class AuthorizationHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        MyMessage mess = new MyMessage();
        String str = mess.formReceiveMsg(in);
        System.out.println(str);
        String[] token = str.split(" ");
        System.out.println(Arrays.toString(token));
        if (str.startsWith("/auth")) {
            String nickName = SqlWorker.getNickByLoginAndPass(token[1], token[2]);
            ctx.pipeline().addLast(new MainServerHandler(nickName));
            ctx.pipeline().remove(this);
            ctx.fireChannelActive();
        } else {
            System.out.println("Вы ввели не верный логин или пароль!!!");
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
