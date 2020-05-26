package serverapp;

import common.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
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
    public Channel currentChannel;

    public Worker(String nickName, ChannelHandlerContext ctx, boolean isAuth) throws IOException, InterruptedException {
        this.nickName = nickName;
        this.ctx = ctx;
        this.isAuth = isAuth;

        // если ник != null значит авторизация прошла успешно
        if (nickName != null && isAuth) {
            Server.subscribe(this);     // помещаем информацию о пользователе в vector clients
            MyCommandSend.sendCommand("/authOK " + nickName, ctx.channel());    // посылаем клиенту ник авторизованого пользователя
            Thread.sleep(500);  // делаем интервал м\у двумя служебными сообщениями
            for (Worker o: Server.clients) {
                MyCommandSend.sendCommand("/respClientList " + clientList(), o.getCtx().channel()); // пробегаем клиентов и рассылаем им обновленный список активных пользователей
            }
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
                    // блок обработки запроса и отправки списка файлов клиенту
                    if (message.equals("/req_list")) {
                        MyCommandSend.sendCommand("/req_list " + preSplit(), this.ctx.channel());
                    }
                    // блок удаления файлов на сервере
                    if (message.startsWith("/delete")) {
                        String[] strSplit = message.split(" ");
                        Files.delete(Paths.get("server_storage/" + strSplit[1]));
                        System.out.println("Файл " + strSplit[1] + " удален пользователем " + nickName + "!!!");
                    }
                    // блок откючения клиента
                    if (message.startsWith("/authOFF")) {
                        Server.unsubscribe(this);
                        ctx.channel().close();
                        for (Worker o: Server.clients) {
                            MyCommandSend.sendCommand("/respClientList " + clientList(), o.getCtx().channel());
                        }
                    }
                    // блок обработки запроса списка активных пользователей и отправки их клиенту
//                    if (message.equals("/clientList")) {
//                        MyCommandSend.sendCommand("/respClientList " + clientList(), ctx.channel());
//                    }
                    if (message.startsWith("/msgAll")) {
                        currentChannel = this.ctx.channel();
                        String[] strSplit = message.split("&");
                        MyCommandSend.sendCommand("/confReceive", this.ctx.channel());
                        sendMsgAll(strSplit[1]);
                    }
                    System.out.println("From client = " + message);
                }
    }

    public void sendMsgAll(String message) throws IOException {
        String nick = "";
        for (Worker o: Server.clients) {
           if (o.getCtx().channel() == currentChannel) {
               nick = o.getNickName();
           }
        }
        for (Worker o: Server.clients) {
            if (o.getCtx().channel() != currentChannel) {
                MyCommandSend.sendCommand("/all&" + nick + "&" + message, o.ctx.channel());
            }
        }
    }

    // собираем строку со списком активных пользователей для отправки клиентам
    public String clientList() {
        StringBuilder clientSB = new StringBuilder();
        for (Worker o : Server.clients) {
            clientSB.append(o.getNickName()).append(" ");
        }
        return clientSB.toString();
    }

    // составляем строку списка файлов для отправки клиенту
    public String preSplit() throws IOException {
        StringBuilder listSB = new StringBuilder();
        List<String> tmpList = MyFileList.listFile("server_storage");
        for (String o : tmpList) {
            listSB.append(o).append(" ");
        }
        return listSB.toString();
    }
}
