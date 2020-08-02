package com.cyz.socks5.server.host;

public interface HostAddr {
    byte[] toBytes(String host);

    String resolve(byte[] bytes);
}
