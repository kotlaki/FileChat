package serverapp;

import common.MyCommandReceive;
import common.MyCommandSend;
import common.MyFileReceive;
import common.MyFileSend;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/*
    /fr - file receive - получение файла
    /fs - file send - отправка файла
    /reg login password nickname description - регистрация нового пользователя

*/

public class ServerHandler extends ChannelInboundHandlerAdapter {

    public Worker worker;

    public ServerHandler(String nickName, ChannelHandlerContext ctx, boolean isAuth) throws IOException, InterruptedException {
        worker = new Worker(nickName, ctx, isAuth);
    }

    private String nickName;

    public ServerHandler(String nickName) {
        this.nickName = nickName;
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
