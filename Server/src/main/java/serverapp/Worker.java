package serverapp;

import common.MyFileReceive;
import common.MyFileSend;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class Worker {
    public String str;
    public String prev = "";
    private String nickName;
    private ChannelHandlerContext ctx;
    private boolean isAuth;

    public Worker(String nickName, ChannelHandlerContext ctx, boolean isAuth) {
        this.nickName = nickName;
        this.ctx = ctx;
        this.isAuth = isAuth;

        if (nickName != null && isAuth) {
            Server.subscribe(this);
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(nickName, CharsetUtil.UTF_8));
        }
    }

    public String getNickName() {
        return nickName;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void workMsg(ChannelHandlerContext ctx, ByteBuf in) throws IOException {
    // обрабатываем первую отправку чтобы узнать наличие /file в строке
        if (!prev.equals("/fs")) {
            str = in.toString(CharsetUtil.UTF_8);
            // обрабатываем завершение работы клиента
            if (str.startsWith("/end")) {
                System.out.println(this.nickName + " покинул чат...");
                // удаление из списка worker
                Server.unsubscribe(this);
                ctx.channel().close();
            }
            System.out.println(nickName + " написал: " + str);
            if (str.startsWith("/fr")) {
                String[] strSplit = str.split(" ");
                MyFileSend.sendFile(Paths.get("server_storage/" + strSplit[1]), ctx.channel(), future -> {
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
        if (prev.equals("/fs")) {
            MyFileReceive.receiveFile(in, "server_storage/");
            prev = "";
        }
        // после получении строки проверяем начинается ли она с /file
        if (str.startsWith("/fs")) {
            // помечаем что предидущая строка содержала /file
            prev = "/fs";
        }
    }
}
