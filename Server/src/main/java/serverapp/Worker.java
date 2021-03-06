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
            for (Worker o : Server.clients) {
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

        // приемка файла
        if (str.startsWith("/file") || MyFileReceive.currentState == MyFileReceive.State.FILE) {
            MyFileReceive.receiveFile(in, "server_storage/" + getNickName() + "/");
            if (MyFileReceive.currentState == MyFileReceive.State.IDLE){
                MyCommandSend.sendCommand("/receiveFileOK", this.ctx.channel());
            }
        } else
            // приемка сообщений
            if (str.startsWith("/message") || MyCommandReceive.currentState == MyCommandReceive.State.MESSAGE) {
                String message = MyCommandReceive.receiveCommand(in);
                // обработка запроса на файл с последующей передачей файла клиенту
                if (message.startsWith("/fr")) {
                    String[] strSplit = str.split(" ");
                    MyFileSend.sendFile(Paths.get(strSplit[1]), ctx.channel());
                }
                // блок обработки запроса и отправки списка файлов клиенту
                if (message.equals("/req_list")) {
                    MyCommandSend.sendCommand("/req_list&&" + preSplit(), this.ctx.channel());
                }
                // блок удаления файлов на сервере
                if (message.startsWith("/delete")) {
                    String[] strSplit = message.split(" ");
                    Files.delete(Paths.get("server_storage/" + getNickName() + "/" + strSplit[1]));
                    System.out.println("Файл " + strSplit[1] + " удален пользователем " + nickName + "!!!");
                    MyCommandSend.sendCommand("/deleteOK", this.ctx.channel());
                }
                // блок откючения клиента
                if (message.startsWith("/authOFF")) {
                    Server.unsubscribe(this);
                    ctx.channel().close();
                    for (Worker o : Server.clients) {
                        MyCommandSend.sendCommand("/respClientList " + clientList(), o.getCtx().channel());
                    }
                }
                // блок обработки запроса списка активных пользователей и отправки их клиенту
                if (message.startsWith("/msgAll")) {
                    currentChannel = this.ctx.channel();
                    String[] strSplit = message.split("&");
                    MyCommandSend.sendCommand("/confReceiveAllMsg", this.ctx.channel());
                    sendMsgAll(strSplit[1]);
                }
                // блок обработки приватных сообщений
                if (message.startsWith("/pm")) {
                    currentChannel = this.ctx.channel();
                    String[] strSplit = message.split("&");
                    String[] strGetMsg = strSplit[2].split(":-");   // отделяем сообщение от остальной части
                    if (userIsActive(strSplit[1])) {
                        MyCommandSend.sendCommand("/confReceivePrivate", currentChannel);
                        sendPrivateMsg(strSplit[1], strGetMsg[1]);
                    } else {
                        // TODO: 29.05.2020 сделать передачу сообщения клиенту
                        System.out.println("Пользователь в данный момент не активен!!!");
                    }
                }

                System.out.println("From client = " + message);
            }
    }

    // метод отправки приватного сообщения получателю
    public void sendPrivateMsg(String nickReceiver, String message) throws IOException {
        MyCommandSend.sendCommand("/pm&" + this.getNickName() + "&" + message, getChannelReceiver(nickReceiver));
    }

    // метод отправки сообщений для всех
    public void sendMsgAll(String message) throws IOException {
        String nick = "";
        for (Worker o : Server.clients) {
            if (o.getCtx().channel() == currentChannel) {
                nick = o.getNickName();
            }
        }
        for (Worker o : Server.clients) {
            if (o.getCtx().channel() != currentChannel) {
                MyCommandSend.sendCommand("/all&" + nick + "&" + message, o.ctx.channel());
            }
        }
    }


    // узнаем канал получателя
    public Channel getChannelReceiver(String nickReceiver) {
        for (Worker o : Server.clients) {
            if (o.getNickName().equals(nickReceiver)) {
                return o.getCtx().channel();
            }
        }
        return null;
    }

    // узнаем активность пользователя
    public boolean userIsActive(String nick) {
        for (Worker o: Server.clients) {
            if (o.getNickName().equals(nick)) return true;
        }
        return false;
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
        List<String> tmpList = MyFileList.listFile("server_storage/" + getNickName());
        for (String o : tmpList) {
            listSB.append(o).append("&&");
        }
        return listSB.toString();
    }
}
