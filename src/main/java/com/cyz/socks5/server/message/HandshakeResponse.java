package com.cyz.socks5.server.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 握手响应: version||method
 */
public class HandshakeResponse implements SocksMessage {

    private byte version;

    private byte method;

    @Override
    public void serialize(OutputStream os) throws IOException {
        os.write(version);
        os.write(method);
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        int version = is.read();
        int method = is.read();
        this.version = (byte)version;
        this.method = (byte)method;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getMethod() {
        return method;
    }

    public void setMethod(byte method) {
        this.method = method;
    }
}
