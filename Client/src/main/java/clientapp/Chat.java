package clientapp;

import common.MyCommandSend;
import common.MyFileReceive;
import common.MyFileSend;
import io.netty.buffer.ByteBuf;
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

    public void chat(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {

        System.out.print("Enter message: ");
        Scanner scanner = new Scanner(System.in);
        str = new String(scanner.nextLine());

        // блок отправки файла на сервер
        if (str.startsWith("/fs")) {
            String[] token = str.split(" ");
            str = "";
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

        // блок отправки сообщений
        if (!prev.equals("/fr")) {
            if (!str.equals("")) {
                MyCommandSend.sendCommand(str, ctx.channel());
                test = buf.toString(CharsetUtil.UTF_8);
            }
        }

        // блок принития файла с сервера
        if (prev.equals("/fr")) {
            MyFileReceive.receiveFile(buf, "client_storage/");
            prev = "";
        }
        if (str.startsWith("/fr")) {
            prev = "/fr";
        }

    }
}
