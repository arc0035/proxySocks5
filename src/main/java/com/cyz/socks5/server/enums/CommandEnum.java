package com.cyz.socks5.server.enums;

public enum  CommandEnum {

    CONNECT((byte)0x01),
    BIND((byte)0x02),
    UDP((byte)0x03);

    private byte code;

    CommandEnum(byte code){
        this.code = code;
    }

    public static CommandEnum fromCode(byte opcode) {
        for(CommandEnum cmd: CommandEnum.values()){
            if(cmd.code == opcode){
                return cmd;
            }
        }
        return null;
    }
}
