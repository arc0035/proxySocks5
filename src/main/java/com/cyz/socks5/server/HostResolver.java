package com.cyz.socks5.server;

import com.cyz.socks5.server.enums.ClientErrorEnum;
import com.cyz.socks5.server.error.ProxyServerException;

public class HostResolver {

    //不做扩展性考虑，所以用if else
    public String resolveHost(byte addrType, byte[] dstAddr){
        //0x01-ipv4; 0x03-domain; 0x04-ipv6
        if(addrType == 0x01){
            return resolveIpv4(dstAddr);
        }
        else if(addrType == 0x03){
            return resolveDomain(dstAddr);
        }
        else if(addrType == 0x04){
            return resolveIpv6(dstAddr);
        }
        throw new ProxyServerException(ClientErrorEnum.InvalidCmdOpcode);
    }

    private String resolveIpv4(byte[] dstAddr) {
       return null;
    }

    private String resolveDomain(byte[] dstAddr) {

        return null;
    }


    private String resolveIpv6(byte[] dstAddr){
        return null;
    }





}
