package com.cyz.socks5.server.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 命令。 version||command||rsv||addressType||dst.addr||dst.port
 */
public class CommandRequest implements SocksMessage{

    /**
     * 版本号
     */
    private byte version;

    /**
     * 命令，包括CONNECT, BIND, UDP
     */
    private byte cmd;

    /**
     * 闲置字段
     */
    private byte rsv;

    /**
     * 地址类型
     */
    private byte addressType;

    /**
     * 目标主机地址
     */
    private byte[] dstAddr;

    /**
     * 目标主机端口
     */
    private short dstPort;

    @Override
    public void serialize(OutputStream os) throws IOException {
        os.write(this.version);

    }

    @Override
    public void deserialize(InputStream is) throws IOException {

    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public byte getRsv() {
        return rsv;
    }

    public void setRsv(byte rsv) {
        this.rsv = rsv;
    }

    public byte getAddressType() {
        return addressType;
    }

    public void setAddressType(byte addressType) {
        this.addressType = addressType;
    }

    public byte[] getDstAddr() {
        return dstAddr;
    }

    public void setDstAddr(byte[] dstAddr) {
        this.dstAddr = dstAddr;
    }

    public short getDstPort() {
        return dstPort;
    }

    public void setDstPort(short dstPort) {
        this.dstPort = dstPort;
    }
}
