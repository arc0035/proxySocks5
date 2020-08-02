package com.cyz.socks5.server.error;

import com.cyz.socks5.server.enums.ClientErrorEnum;

public class ProxyServerException extends RuntimeException{

    private  ClientErrorEnum errorEnum;

    public ProxyServerException(ClientErrorEnum errorEnum){
        super(errorEnum.getMessage());
        this.errorEnum = errorEnum;
    }

    public ClientErrorEnum getErrorEnum() {
        return errorEnum;
    }
}
