package serverapp;

import common.MyCommandReceive;
import common.MyFileReceive;
import common.MyFileSend;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.file.Paths;

/*
    /fr - file receive - получение файла
    /fs - file send - отправка файла
    /reg login password nickname description - регистрация нового пользователя

*/

public class MainServerHandler extends ChannelInboundHandlerAdapter {

    private String nickName;
    private String str;
    private String prev = "";

    public MainServerHandler(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // прошли авторизацию, сообщили клиенту отправкой служебного сообщения
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(nickName, CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //TODO сделать отправку больших сообщений больше 1024 символа
        ByteBuf in = (ByteBuf) msg;
        byte firstByte = in.readByte();
        if (firstByte == 77) {
            in.readerIndex(0);
            MyCommandReceive.receiveCommand(in);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
