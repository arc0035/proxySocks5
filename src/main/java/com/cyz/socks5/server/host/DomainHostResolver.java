package com.cyz.socks5.server.host;

import java.util.Arrays;

public class DomainHostResolver implements HostAddrResolver {
    @Override
    public byte[] toBytes(String host) {
        byte[] hostBytes = host.getBytes();
        int length = hostBytes.length;
        byte[] result = new byte[1+length];
        result[0] = (byte)length;
        System.arraycopy(hostBytes, 0, result, 1, length);
        return result;
    }

    @Override
    public String resolve(byte[] bytes) {
        String domainName = new String(Arrays.copyOfRange(bytes, 1, bytes.length));
        return domainName;
    }
}
