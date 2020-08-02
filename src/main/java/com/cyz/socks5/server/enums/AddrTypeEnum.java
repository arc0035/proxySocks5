package com.cyz.socks5.server.enums;

/**
 * 地址类型
 */
public enum AddrTypeEnum {

    IPV4(0x01),
    DOMAIN(0x03),
    IPV6(0x04);

    private int code;

    AddrTypeEnum(int code){
        this.code = (byte)code;
    }

    public int getCode() {
        return code;
    }
}
