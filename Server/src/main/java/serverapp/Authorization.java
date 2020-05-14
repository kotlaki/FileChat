package serverapp;

import io.netty.channel.ChannelHandlerContext;

public class Authorization {
    public boolean isAuth = false;
    public void checkUser(String str, ChannelHandlerContext ctx) {
        String[] token = str.split(" ");
        if (str.startsWith("/auth")) {
            try {
                String nickName = SqlWorker.getNickByLoginAndPass(token[1], token[2]);
                if (nickName == null) {
                    System.out.println("Не верные данные авторизации пользователя!!! Проверте логин и пароль...");
                    ctx.close();
                } else {
                    System.out.println("User " + nickName + " good!");
                    isAuth = true;
                    ctx.pipeline().addLast("msh", new MainServerHandler(nickName));
                    ctx.pipeline().remove("authorization");
                    ctx.fireChannelActive();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Проверьте правильность ввода логина и пароля!!!");
            }
        } else {
            System.out.println("Вы ввели не верный логин или пароль!!!");
            ctx.close();
        }
    }
}
