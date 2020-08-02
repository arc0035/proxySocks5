package com.cyz.socks5.server.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SocksMessage {

    void serialize(OutputStream os) throws IOException;

    void deserialize(InputStream is) throws IOException;
}
