package me.xiao.chatbot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 主程序入口，启动http服务
 *
 * @author BaoQiang
 * @version 2.0
 * @Create at 2016/11/4 23:28
 */
public class Searcher {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .handler(new LoggingHandler(LogLevel.TRACE))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("http-decoder", new HttpRequestDecoder());
                        p.addLast("http-aggregator", new HttpObjectAggregator(65535));
                        p.addLast("http-encoder", new HttpRequestEncoder());
                        p.addLast("handler", new HttpServerInboundHandler());
                    }
                });

        ChannelFuture f = b.bind("127.0.0.1", 8765).sync();
        f.channel().closeFuture().sync();


    }
}
