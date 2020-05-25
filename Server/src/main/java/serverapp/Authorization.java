package serverapp;

import common.MyCommandSend;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

import java.io.IOException;

public class Authorization {
    public static boolean isAuth = false;
    public static boolean equalsNick = false;

    public static void checkUser(String str, ChannelHandlerContext ctx) throws IOException {
        isAuth = false;
        String[] token = str.split(" ");
        if (str.startsWith("/auth")) {
            try {
                String nickName = SqlWorker.getNickByLoginAndPass(token[1], token[2]);
                if (nickName == null) {
                    System.out.println("Не верные данные авторизации пользователя!!!");
                    MyCommandSend.sendCommand("/errorAuth&Не верные данные авторизации " +
                            "пользователя!!! Проверте логин и пароль...", ctx.channel());
                    ctx.close();
                } else {
                    // пользователь с таким ником уже вошел?
                    for (Worker o: Server.clients) {
                        if (o.getNickName().equals(nickName)) {
                            equalsNick = true;
                            System.out.println("Повторный вход пользователя!!!");
                            ctx.channel().writeAndFlush(Unpooled.copiedBuffer("/close", CharsetUtil.UTF_8));
                            break;
                        }
                    }
                    System.out.println("User, " + nickName + ", register!");
                    if (!equalsNick) {
                        isAuth = true;
                    }
                    equalsNick = false;
                    ctx.pipeline().addLast("msh", new ServerHandler(nickName, ctx, isAuth));
                    ctx.pipeline().remove("authorization");
                    ctx.fireChannelActive();
                }
            } catch (ArrayIndexOutOfBoundsException | IOException e) {
                System.out.println("Пользователь ввел не верный логин и пароль!!!");
//                ctx.channel().writeAndFlush(Unpooled.copiedBuffer(
//                        "Проверьте правильность ввода логина и пароля!!!", CharsetUtil.UTF_8));
                MyCommandSend.sendCommand("/errorAuth&Пользователь ввел не верный логин и пароль!!!", ctx.channel());
            }
        } else {
            System.out.println("Пользователь ввел не верный логин или пароль!!!");
//            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(
//                    "Вы ввели не верный логин или пароль!!!", CharsetUtil.UTF_8));
            MyCommandSend.sendCommand("/errorAuth&Вы ввели не верный логин или пароль!!!", ctx.channel());
            ctx.close();
        }
    }
    public static String addUser(String str, ChannelHandlerContext ctx) {
        String result = null;
        try {
            String[] token = str.split(" ");
            result = SqlWorker.addUser(token[2], token[3], token[4], token[5]);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("При регистрации нового пользователя пришли не валидные данные!!!");
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer("Проверьте правильность ввода данных!!!" +
                    " (Example: login password nickname description)", CharsetUtil.UTF_8));
        }
        return result;
    }
}
