package com.cyz.socks5.server.error;

import com.cyz.socks5.server.enums.CommonErrorEnum;

public class ProxyServerException extends RuntimeException{

    private CommonErrorEnum errorEnum;

    public ProxyServerException(CommonErrorEnum errorEnum){
        super(errorEnum.getMessage());
        this.errorEnum = errorEnum;
    }

    public CommonErrorEnum getErrorEnum() {
        return errorEnum;
    }
}
