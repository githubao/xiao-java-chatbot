package me.xiao.chatbot;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;

public class NettyHttpServletResponse extends DefaultHttpResponse implements FullHttpResponse {

    private ByteBuf content;

    public NettyHttpServletResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }

    public HttpHeaders trailingHeaders() {
        return null;
    }

    public void setContent(ByteBuf buf) {
        this.content = buf;
    }

    public ByteBuf content() {
        return content;
    }

    public int refCnt() {
        return 0;
    }

    public boolean release() {
        return false;
    }

    public boolean release(int decrement) {
        return false;
    }

    public FullHttpResponse copy(ByteBuf newContent) {
        return null;
    }

    public FullHttpResponse copy() {
        return null;
    }

    public FullHttpResponse retain(int increment) {
        return null;
    }

    public FullHttpResponse retain() {
        return null;
    }

    public FullHttpResponse touch() {
        return null;
    }

    public FullHttpResponse touch(Object hint) {
        return null;
    }

    public FullHttpResponse duplicate() {
        return null;
    }

    public FullHttpResponse setProtocolVersion(HttpVersion version) {
        return null;
    }

    public FullHttpResponse setStatus(HttpResponseStatus status) {
        return null;
    }

}
