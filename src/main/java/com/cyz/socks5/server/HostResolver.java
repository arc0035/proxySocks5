package com.cyz.socks5.server;

import com.cyz.socks5.server.enums.ClientErrorEnum;
import com.cyz.socks5.server.error.ProxyServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class HostResolver {

    private static final Logger logger = LoggerFactory.getLogger(HostResolver.class);

    public byte[] hostToBytes(int addrType, String host){
        if (addrType == 0x01) {
            return ipv4ToBytes(host);
        } else if (addrType == 0x03) {
            return domainToBytes(host);
        } else if (addrType == 0x04) {
            return ipv6ToBytes(host);
        }
        return null;
    }

    private byte[] ipv4ToBytes(String host) {
        int pos = 0;
        byte[] result = new byte[4];
        String[] parts = host.split("\\.");
        for(String part: parts){
            int data = Integer.parseInt(part);
            result[pos++] = (byte)data;
        }
        return result;
    }

    private byte[] ipv6ToBytes(String host) {
        return null;
    }

    private byte[] domainToBytes(String host) {
        return null;
    }



    //不做扩展性考虑，所以用if else
    public String resolveHost(int addrType, byte[] dstAddr) {
        //0x01-ipv4; 0x03-domain; 0x04-ipv6
        try {
            if (addrType == 0x01) {
                return resolveIpv4(dstAddr);
            } else if (addrType == 0x03) {
                return resolveDomain(dstAddr);
            } else if (addrType == 0x04) {
                return resolveIpv6(dstAddr);
            }
            throw new ProxyServerException(ClientErrorEnum.InvalidAddressType);
        } catch (ProxyServerException pse) {
            throw pse;
        }
        catch (Exception ex){
            logger.warn("{}", ex.getMessage());
            return null;
        }
    }

    private String resolveIpv4(byte[] dstAddr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int part = dstAddr[i] & 0xFF;
            sb.append(part);
            sb.append(".");
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

    private String resolveDomain(byte[] dstAddr) {
        String domainName = new String(Arrays.copyOfRange(dstAddr, 1, dstAddr.length));
        return domainName;
    }

    private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    private String resolveIpv6(byte[] dstAddr){
        //16 bytes ipv6. 2 bytes a bundle
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<dstAddr.length;i+=2){
            char ch1 = HEX_CHARS[(dstAddr[i] >> 4) & 0x0F];
            char ch2 = HEX_CHARS[dstAddr[i] & 0x0F];
            char ch3 = HEX_CHARS[(dstAddr[i+1] >> 4) & 0x0F];
            char ch4 = HEX_CHARS[dstAddr[i+1] & 0x0F];
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
