package com.cyz.socks5.server.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 认证结果。version||result
 */
public class AuthenticationResultResponse implements SocksMessage{

    public static final byte SUCCESS = 0x00;

    private byte version;

    private byte result;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    @Override
    public void serialize(OutputStream os) throws IOException {
        os.write(this.version);
        os.write(this.result);
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        this.version = (byte)is.read();
        this.result = (byte)is.read();
    }
}
