package clientapp;

import common.MyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.Scanner;

public class MainClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // блок отправки данных авторизации пользователя
        System.out.print("Enter login and password: ");
        Scanner scanner = new Scanner(System.in);
        String str = new String("/auth " + scanner.nextLine());
        // запаковываем данные
        MyMessage myMsg = new MyMessage(str);
        myMsg.formSendMsg(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // принимаем служебные данные от сервера
        ByteBuf buf = (ByteBuf) msg;
        String nickName = buf.toString(CharsetUtil.UTF_8);
        buf.release();
        new Chat(nickName, ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
