package com.cyz.socks5.server.enums;

/**
 * 密码认证方案
 */
public enum AuthenticationMethod {

    NONE("none", 0x00),
    GSSAPI("gssapi", 0x01),
    USERPASSWORD("pwd", 0x02),
    NOTHING("nothing", 0xFF);

    private String name;
    private int code;

    AuthenticationMethod(String method, int code){
        this.name = method;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public static AuthenticationMethod fromName(String name){
        for(AuthenticationMethod authMethod: AuthenticationMethod.values()){
            if(authMethod.getName().compareToIgnoreCase(name) == 0){
                return authMethod;
            }
        }
        throw new IllegalArgumentException("Unrecognized authenticatio method "+ name);
    }


}
