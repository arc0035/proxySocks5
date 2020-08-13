package com.cyz.socks5.server.enums;

public enum CommonErrorEnum {

    InvalidVersion("invalid socket version, 0x05 is required"),
    BadAuthenticationMethodCount("must have a valid authentication method count"),
    NoSupportAuthenticationMethod("no supported authentication method"),
    AuthenticationFailed("authentication failed, invalid client"),
    InvalidCmdOpcode("Invalid command operation code"),
    InvalidAddressType("Invalid address type"),
    SelectorFailed("Selector failed");

    private String message;

    CommonErrorEnum(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
