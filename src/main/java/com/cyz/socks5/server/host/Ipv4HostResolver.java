package com.cyz.socks5.server.host;

public class Ipv4HostResolver implements HostAddrResolver {

    @Override
    public byte[] toBytes(String host) {
        int pos = 0;
        byte[] result = new byte[4];
        String[] parts = host.split("\\.");
        for(String part: parts){
            int data = Integer.parseInt(part);
            result[pos++] = (byte)data;
        }
        return result;
    }

    @Override
    public String resolve(byte[] dstAddr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int part = dstAddr[i] & 0xFF;
            sb.append(part);
            sb.append(".");
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }
}
