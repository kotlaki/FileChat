package serverapp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Vector;

public class Server {
    public static Vector<Worker> clients = new Vector<>();
    public void run() {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup work = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            SqlWorker.connect();
            b.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("authorization", new AuthorizationHandler());
                        }
                    });
            ChannelFuture f = b.bind(8189).sync();
//            clients = new Vector<>();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            work.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    public static void subscribe(Worker client) {
        clients.add(client);
    }

    public static void unsubscribe(Worker client) {
        clients.remove(client);
//        broadcastClientsList();
    }
}