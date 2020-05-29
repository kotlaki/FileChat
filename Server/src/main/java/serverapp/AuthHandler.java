package serverapp;

import common.MyCommandReceive;
import common.MyCommandSend;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        String str = MyCommandReceive.receiveCommand(in);
        // блок регистрации и авторизации пользователей
        if (str.startsWith("/regNewUser")) {
            // регистрируем нового пользователя и отправляем ответ клиенту
            MyCommandSend.sendCommand("/respReg&" + Authorization.addUser(str, ctx), ctx.channel());
        } else {
            // авторизируем пользователей
            Authorization.checkUser(str, ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
