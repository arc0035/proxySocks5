package com.cyz.socks5.server.enums;

public enum CommandTypeEnum {

    CONNECT(0x01),
    BIND(0x02),
    UDP(0x03),

    UNKNOWN(-1);

    private int code;

    CommandTypeEnum(int code){
        this.code = code;
    }

    public int getCode(){
        return this.code;
    }


    public static CommandTypeEnum fromCode(int opcode) {
        for(CommandTypeEnum cmd: CommandTypeEnum.values()){
            if(cmd.code == opcode){
                return cmd;
            }
        }
        return CommandTypeEnum.UNKNOWN;
    }
}
