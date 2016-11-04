package me.xiao.chatbot;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * http 服务处理
 *
 * @author BaoQiang
 * @version 2.0
 * @Create at 2016/11/4 23:10
 */
public class HttpServerInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        NettyHttpServletResponse res = new NettyHttpServletResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        Action.doServlet(req, res);
        ChannelFuture future = ctx.channel().writeAndFlush(res);
        future.addListener(ChannelFutureListener.CLOSE);
    }
}
