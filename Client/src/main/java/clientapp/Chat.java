package clientapp;

import common.MyFileSend;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Chat {

    public Chat(String nickName, ChannelHandlerContext ctx) throws IOException {
        System.out.println("Hello " + nickName + "!!!");
        System.out.print("Enter message: ");
        Scanner scanner = new Scanner(System.in);
        String str = new String(scanner.nextLine());

        // запаковываем данные
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8));

        if (str.startsWith("/file")) {
            String[] token = str.split(" ");
            String pathToFile = token[1];
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
