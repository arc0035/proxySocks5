package com.cyz.socks5.server.util;

import java.io.IOException;
import java.io.InputStream;

public class CommandUtil {

    public static byte[] readIpv4(InputStream is) throws IOException {
        return readFully(is, 4);
    }

    public static byte[] readDomain(InputStream is) throws IOException {
        int length = is.read();
        byte[] domainData = readFully(is, length);
        byte[] result = new byte[length+1];
        result[0] = (byte)length;
        System.arraycopy(domainData,0, result, 1, length);
        return result;
    }

    public static byte[] readIpv6(InputStream is) throws IOException {
        return readFully(is, 16);
    }
    public static byte[] readFully(InputStream is, int length) throws IOException{
        byte[] bytes = new byte[length];
        int nread= 0 ;
        while (nread < length){
            nread += is.read(bytes, nread, length - nread);
        }
        return bytes;
    }

    private CommandUtil(){}

}
