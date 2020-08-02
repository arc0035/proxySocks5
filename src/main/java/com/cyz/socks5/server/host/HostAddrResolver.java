package com.cyz.socks5.server.host;

public interface HostAddrResolver {
    byte[] toBytes(String host);

    String resolve(byte[] bytes);
}
