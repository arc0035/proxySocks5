package com.cyz.socks5.server.host;

import java.util.HashMap;
import java.util.Map;

public class Ipv6HostResolver implements HostAddrResolver {
    private final static char[] HEX_CHARS;

    static {
        HEX_CHARS = "0123456789ABCDEF".toCharArray();
    }
    //abcd:abcd:
    @Override
    public byte[] toBytes(String host) {
        byte[] bytes = new byte[16];
        int charLen = host.length();
        int pos = 0;
        for(int i=0;i<charLen;i+=5){
            int val1 = decode(host.charAt(i));
            int val2 = decode(host.charAt(i+1));
            int val3 = decode(host.charAt(i+2));
            int val4 = decode(host.charAt(i+3));

            byte b1 = (byte)(val1 << 4 | val2);
            byte b2 = (byte)(val3 << 4 | val4);
            bytes[pos++] = b1;
            bytes[pos++] = b2;
        }
        return bytes;
    }

    private int decode(char ch){
        if(ch >= '0' && ch <= '9'){
            return ch - '0';
        }
        if(ch >= 'A' && ch <='F'){
            return ch - 'A' + 10;
        }
        if(ch >= 'a' && ch <='f'){
            return ch - 'a' + 10;
        }
        return -1;
    }


    @Override
    public String resolve(byte[] bytes) {
        //16 bytes ipv6. 2 bytes a bundle
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<bytes.length;i+=2){
            char ch1 = HEX_CHARS[(bytes[i] >> 4) & 0x0F];
            char ch2 = HEX_CHARS[bytes[i] & 0x0F];
            char ch3 = HEX_CHARS[(bytes[i+1] >> 4) & 0x0F];
            char ch4 = HEX_CHARS[bytes[i+1] & 0x0F];
            sb.append(ch1);
            sb.append(ch2);
            sb.append(ch3);
            sb.append(ch4);
            sb.append(":");
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }
}
