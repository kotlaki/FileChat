package serverapp;

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
        ByteBuf in = (ByteBuf) msg;
        // обрабатываем первую отправку чтобы узнать наличие /file в строке
        if (!prev.equals("/fs")) {
            str = in.toString(CharsetUtil.UTF_8);
            System.out.println(nickName + " написал: " + str);
            if (str.startsWith("/fr")) {
                String[] strSplit = str.split(" ");
                MyFileSend.sendFile(Paths.get(strSplit[1]), ctx.channel(), future -> {
                    if (!future.isSuccess()) {
                        future.cause().printStackTrace();
                    }

                    if (future.isSuccess()) {
                        System.out.println("Файл успешно передан");
                    }
                });
            }
        }
        // т.к предидущая строка содержала /file то обрабатываем прием файла, после чего скидываем предидущий показатель на ""
        if(prev.equals("/fs")) {
            MyFileReceive.receiveFile(in);
            prev = "";
        }
        // после получении строки проверяем начинается ли она с /file
        if (str.startsWith("/fs")) {
            // помечаем что предидущая строка содержала /file
            prev = "/fs";
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
