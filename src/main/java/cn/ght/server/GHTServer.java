package cn.ght.server;

import cn.ght.util.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

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
            bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(4096));
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    //加入SSL
                    SSLEngine engine = GHTSSLContextFactory.getServerContext().createSSLEngine();
                    engine.setUseClientMode(false);
                    engine.setNeedClientAuth(false);
                    ChannelPipeline channelPipeline = socketChannel.pipeline();
                    channelPipeline.addFirst("ssl", new SslHandler(engine));
                    channelPipeline.addLast("encoder", new GHTEncoder());
                    channelPipeline.addLast("decoder", new GHTDecoder());
                    channelPipeline.addLast(new GHTHandler());
                }
            });
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            if (channelFuture.isSuccess()) {
                LogUtils.print("@@@@----GHT Server Start----");
            }
            channelFuture.channel().closeFuture().sync();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
            LogUtils.print("@@@@----GHT Server Stop----");
        }
    }


    public static void main(String[] args) throws InterruptedException {
        int serverPort;
        if (args.length > 0) {
            LogUtils.print("@@@@port:" + args[0]);
            serverPort = Integer.parseInt(args[0]);
        } else {
            LogUtils.print("@@@@no port defined, user default 9001");
            serverPort = 9001;
        }
        GHTServer server = new GHTServer(serverPort);
        server.start();
    }
}
