package serverapp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.util.Vector;

public class Server {
    public static Vector<ServerHandler> clients = new Vector<>();  // тут храним информацию о авторизированных пользователях
    public void run() {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup work = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            SqlWorker.connect();        // подключаемся к БД
            b.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("authorization", new AuthHandler());
                        }
                    });
            ChannelFuture f = b.bind(8189).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            SqlWorker.disconnect();     // отключаем БД
            work.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    public static void subscribe(ServerHandler client) throws IOException {
        clients.add(client);    // добавляем в коллекцию авторезированного пользователя
    }

    public static void unsubscribe(ServerHandler client) {
        clients.remove(client); // удаляем из коллекции отключившегося пользователя
    }

    public static void main(String[] args) {
        new Server().run();
    }
}