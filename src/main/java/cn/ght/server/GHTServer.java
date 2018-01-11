package cn.ght.server;

import cn.ght.protocol.MessageData;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;

public class GHTServer {

    /**
     * 端口号
     */
    private int port;

    public GHTServer(int port) {
        this.port = port;
    }

    /**
     * 启动服务器
     */
    public void start() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 128);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline p = socketChannel.pipeline();
                    p.addLast(new ProtobufVarint32FrameDecoder());
                    p.addLast(new ProtobufDecoder(MessageData.Message.getDefaultInstance()));
                    p.addLast(new ProtobufEncoder());
//                    p.addLast(new JsonHandler());
                    p.addLast(new GHTServerHandler());
                }
            });
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            if (channelFuture.isSuccess()) {
                System.out.println("----GHT Server Start----");
            }
            channelFuture.channel().closeFuture().sync();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
            System.out.println("----GHT Server Stop----");
        }
    }


    public static void main(String[] args) throws InterruptedException {
        int serverPort;
        if (args.length > 1) {
            serverPort = Integer.parseInt(args[0]);
        } else {
            serverPort = 9999;
        }
        GHTServer server = new GHTServer(serverPort);
        server.start();
    }
}
