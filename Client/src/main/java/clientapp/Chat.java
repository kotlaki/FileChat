package clientapp;

import common.MyMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.Scanner;

public class Chat {
    private MyMessage myMsg;

    public Chat(String nickName, ChannelHandlerContext ctx) {
        System.out.println("Hello " + nickName + "!!!");
        while (true) {
            System.out.print("Enter message: ");
            Scanner scanner = new Scanner(System.in);
            String str = new String(scanner.nextLine());
            // запаковываем данные
            MyMessage myMsg = new MyMessage(str);
            myMsg.formSendMsg(ctx.channel());
        }
    }
}
