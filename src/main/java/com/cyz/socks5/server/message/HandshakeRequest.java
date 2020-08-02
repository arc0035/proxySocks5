package com.cyz.socks5.server.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 认证握手请求。 格式：version||method_count||methods
 */
public class HandshakeRequest implements SocksMessage {

    /**
     * 协议版本号，为5
     */
    private byte version;

    /**
     * 客户端支持的认证方法数量
     */
    private byte methodCount;

    /**
     * 客户端支持的认证方法列表
     */
    private byte[] methods;


    @Override
    public void serialize(OutputStream os) throws IOException {
        os.write(this.version);
        os.write(this.methodCount);
        os.write(this.methods);
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        int version = is.read();
        int methodCount = is.read();
        byte[] methods = null;
        if(methodCount > 0){
            methods = new byte[methodCount];
            //必须这么写，有可能是分段式的，而不是一次性读完所有的字节
            int nread = 0;
            while(nread < methodCount){
                nread += is.read(methods, nread, methodCount - nread);
            }
        }
        this.version = (byte)version;
        this.methodCount = (byte)methodCount;
        this.methods = methods;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getMethodCount() {
        return methodCount;
    }

    public void setMethodCount(byte methodCount) {
        this.methodCount = methodCount;
    }

    public byte[] getMethods() {
        return methods;
    }

    public void setMethods(byte[] methods) {
        this.methods = methods;
    }
}
