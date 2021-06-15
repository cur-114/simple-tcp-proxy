package me.cur3.simpleproxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SimpleProxy extends ChannelInboundHandlerAdapter {

    private static EventLoopGroup group;
    private static String rHost = null;
    private static int rPort = -1;

    public static void main(String[] args) throws Exception {
        String lHost = "0.0.0.0";
        int lPort = -1;
        for (String s : args) {
            if (s.startsWith("--listenHost=")) {
                lHost = s.split("=")[1];
            } else if (s.startsWith("--listenPort=")) {
                lPort = Integer.valueOf(s.split("=")[1]);
            } else if (s.startsWith("--remoteHost=")) {
                rHost = s.split("=")[1];
            } else if (s.startsWith("--remotePort=")) {
                rPort = Integer.valueOf(s.split("=")[1]);
            }
        }
        if (lPort == -1 || rHost == null || rPort == -1) {
            System.out.println("invalid arguments.");
            return;
        }

        group = new NioEventLoopGroup();

        EventLoopGroup worker, boss;

        worker = new NioEventLoopGroup();
        boss = new NioEventLoopGroup();

        ServerBootstrap bs = new ServerBootstrap();
        bs.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new SimpleProxy());
                    }
                });

        ChannelFuture future;

        if (lHost != null) {
            future = bs.bind(lHost, lPort).sync();
        } else {
            future = bs.bind(lPort).sync();
        }
        System.out.println("proxy started. ("+lHost+":"+lPort+") -> ("+rHost+":"+rPort+")");
        future.channel().closeFuture().sync();
        worker.shutdownGracefully();
        boss.shutdownGracefully();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Bootstrap bs = new Bootstrap();
        bs.channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                    }
                })
                .group(group);
        Channel remote = bs.connect(rHost,rPort).sync().channel();
        ctx.channel().pipeline().remove(SimpleProxy.class);
        remote.pipeline().addLast(new ChannelMirror(ctx.channel()));
        ctx.channel().pipeline().addLast(new ChannelMirror(remote));
    }
}
