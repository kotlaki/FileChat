package clientapp;

import common.MyFileReceive;
import common.MyFileSend;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/*
    /fr - file receive - получение файла
    /fs - file send - отправка файла
    /reg login password nickname description - регистрация нового пользователя
*/

public class Chat {

    private String prev = "";
    private String test;
    private String str;

    public Chat() {
    }

    public static void authorization(ChannelFuture ctx, String login, String password) {
        // блок отправки данных авторизации пользователя
        System.out.println();
        String str = new String("/auth" + " " + login + " " + password);
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8));
    }

    public static void registration(ChannelFuture ctx, String login, String password, String nickName, String description) {
        // блок отправки данных регистрации пользователя
        String str = new String("/auth /reg" + " " + login + " " + password + " " + nickName + " " + description);
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8));
    }

    public void сhat(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {

        if (!prev.equals("/fr")) {
            System.out.print("Enter message: ");
            Scanner scanner = new Scanner(System.in);
            str = new String(scanner.nextLine());
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8));
            test = buf.toString(CharsetUtil.UTF_8);
        }
        // блок принития файла с сервера
        if(prev.equals("/fr")) {
            MyFileReceive.receiveFile(buf, "client_storage/");
            prev = "";
        }
        if (str.startsWith("/fr")) {
            prev = "/fr";
        }


        // блок отправки файла на сервер
        if (str.startsWith("/fs")) {
            String[] token = str.split(" ");
            String pathToFile = "client_storage/" + token[1];
            MyFileSend.sendFile(Paths.get(pathToFile), ctx.channel(), future -> {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }

                if (future.isSuccess()) {
                    System.out.println("Файл успешно передан");
                }
            });
        }
    }
}
