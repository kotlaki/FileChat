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

public class MainServerHandler extends ChannelInboundHandlerAdapter {

    private String nickName;
    private String str;
    private String prev = "";
    private int count = 0;


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

        ByteBuf in = (ByteBuf) msg;
        String str = in.toString(CharsetUtil.UTF_8);

        // обработка запроса на файл
        if (str.startsWith("/fr")) {
            String[] strSplit = str.split(" ");
            MyFileSend.sendFile(Paths.get(strSplit[1]) , ctx.channel(), future -> {
                if (future.isSuccess()) {
                    System.out.println("Файл отправлен...");
                }
                if (!future.isSuccess()) {
                    System.out.println("Ошибка отправки файла!!!");
                }
            });
        }
        else
        // приемка файла
        if (str.startsWith("/file") || MyFileReceive.currentState == MyFileReceive.State.FILE) {
            MyFileReceive.receiveFile(in, "server_storage/");
        } else
        // приемка сообщений
        if (str.startsWith("/message") || MyCommandReceive.currentState == MyCommandReceive.State.MESSAGE) {
            String message = MyCommandReceive.receiveCommand(in);
            System.out.println("From client = " + message);


        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
