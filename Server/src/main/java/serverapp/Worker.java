package serverapp;

import common.MyCommandReceive;
import common.MyFileReceive;
import common.MyFileSend;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class Worker {

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

        String str = in.toString(CharsetUtil.UTF_8);

        // обработка запроса на файл
        if (str.startsWith("/fr")) {
            String[] strSplit = str.split(" ");
            MyFileSend.sendFile(Paths.get(strSplit[1]), ctx.channel(), future -> {
                if (future.isSuccess()) {
                    System.out.println("Файл отправлен...");
                }
                if (!future.isSuccess()) {
                    System.out.println("Ошибка отправки файла!!!");
                }
            });
        } else
            // приемка файла
            if (str.startsWith("/file") || MyFileReceive.currentState == MyFileReceive.State.FILE) {
                MyFileReceive.receiveFile(in, "server_storage/");
            } else
                // приемка сообщений
                if (str.startsWith("/message") || MyCommandReceive.currentState == MyCommandReceive.State.MESSAGE) {
                    String message = MyCommandReceive.receiveCommand(in);
                    System.out.println("From client = " + message);
                }
    }
}
