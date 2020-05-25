package serverapp;

import common.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Worker {

    private String nickName;
    private ChannelHandlerContext ctx;
    private boolean isAuth;

    public Worker(String nickName, ChannelHandlerContext ctx, boolean isAuth) throws IOException {
        this.nickName = nickName;
        this.ctx = ctx;
        this.isAuth = isAuth;

        if (nickName != null && isAuth) {
            Server.subscribe(this);
//            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(nickName, CharsetUtil.UTF_8));
            MyCommandSend.sendCommand("/authOK " + nickName, ctx.channel());
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

        // обработка запроса на файл с последующей передачей файла клиенту
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
                    if (message.equals("/req_list")) {
                        MyCommandSend.sendCommand("/req_list " + preSplit(), this.ctx.channel());
                    }
                    if (message.startsWith("/delete")) {
                        String[] strSplit = message.split(" ");
                        Files.delete(Paths.get("server_storage/" + strSplit[1]));
                        System.out.println("Файл " + strSplit[1] + " удален пользователем " + nickName + "!!!");
                    }
                    // блок откючения клиента
                    if (message.startsWith("/authOFF")) {
//                        String[] strSplit = message.split(" ");
                        Server.unsubscribe(this);
                        ctx.channel().close();
                    }
                    System.out.println("From client = " + message);
                }
    }

    // составляем строку списка файлов для отправки клиенту
    public String preSplit() throws IOException {
        StringBuilder listSB = new StringBuilder();
        List<String> tmpList = MyFileList.listFile("server_storage");
        for (String o: tmpList) {
            listSB.append(o).append(" ");
        }
        return listSB.toString();
    }
}
