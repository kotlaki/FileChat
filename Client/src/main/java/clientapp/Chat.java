package clientapp;

import common.MyFile;
import common.MyMessage;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Chat {
    private MyMessage myMsg;

    public Chat(String nickName, ChannelHandlerContext ctx) throws IOException {
        System.out.println("Hello " + nickName + "!!!");
        System.out.print("Enter message: ");
        Scanner scanner = new Scanner(System.in);
        String str = new String(scanner.nextLine());

        // запаковываем данные
        MyMessage myMsg = new MyMessage(str);
        myMsg.formSendMsg(ctx.channel());

        if (str.startsWith("/file")) {
            String[] token = str.split(" ");
            String pathToFile = token[1];
            MyFile.sendFile(Paths.get(pathToFile), ctx.channel(), future -> {
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
