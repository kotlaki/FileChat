package serverapp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/*
    /fr - file receive - получение файла
    /fs - file send - отправка файла
    /reg login password nickname description - регистрация нового пользователя

*/

public class MainServerHandler extends ChannelInboundHandlerAdapter {

    public Worker worker;

    public MainServerHandler(String nickName, ChannelHandlerContext ctx, boolean isAuth) {
        worker = new Worker(nickName, ctx, isAuth);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        worker.workMsg(ctx, in);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
