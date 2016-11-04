package me.xiao.chatbot;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;

/**
 * netty http 响应
 *
 * @author BaoQiang
 * @version 2.0
 * @Create at 2016/11/4 23:11
 */
public class NettyHttpServletResponse extends DefaultHttpResponse implements FullHttpResponse {
    private ByteBuf content;

    public NettyHttpServletResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }

    public ByteBuf getContent() {
        return content;
    }

    public void setContent(ByteBuf buf) {
        this.content = buf;
    }

    @Override
    public FullHttpResponse copy(ByteBuf byteBuf) {
        return null;
    }

    @Override
    public FullHttpResponse copy() {
        return null;
    }

    @Override
    public FullHttpResponse retain(int i) {
        return null;
    }

    @Override
    public FullHttpResponse retain() {
        return null;
    }

    @Override
    public FullHttpResponse touch() {
        return null;
    }

    @Override
    public FullHttpResponse touch(Object o) {
        return null;
    }

    @Override
    public FullHttpResponse duplicate() {
        return null;
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return null;
    }

    @Override
    public ByteBuf content() {
        return null;
    }

    @Override
    public int refCnt() {
        return 0;
    }

    @Override
    public boolean release() {
        return false;
    }

    @Override
    public boolean release(int i) {
        return false;
    }

    @Override
    public FullHttpResponse setProtocolVersion(HttpVersion version) {
        return null;
    }

    @Override
    public FullHttpResponse setStatus(HttpResponseStatus status) {
        return null;
    }
}
