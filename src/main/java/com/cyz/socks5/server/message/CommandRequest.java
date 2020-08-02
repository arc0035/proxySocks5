package com.cyz.socks5.server.message;

import com.cyz.socks5.server.enums.AddrTypeEnum;
import com.cyz.socks5.server.util.CommandUtil;

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
    private byte version = 0x05;

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
    private int dstPort;

    //TODO:考虑到java没有无符号整数，所以要后续把所有的数据类型都扩容，最好改为int
    //TODO:inputstream需要关心字节的处理，即使是short也要手动处理。后续需要改为DataInputStream
    @Override
    public void serialize(OutputStream os) throws IOException {
        os.write(this.version);
        os.write(this.cmd);
        os.write(this.rsv);
        os.write(this.addressType);
        os.write(this.dstAddr);
        byte[] portBytes = new byte[2];
        portBytes[0] = (byte)(dstPort >> 8);
        portBytes[1] = (byte)dstPort;
        os.write(portBytes);
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        int version = is.read();
        int cmd = is.read();
        int rsv = is.read();
        int addressType = is.read();
        byte[] addr = null;
        if (addressType == AddrTypeEnum.IPV4.getCode()) {
            addr = CommandUtil.readIpv4(is);
        } else if (addressType == AddrTypeEnum.DOMAIN.getCode()) {
            addr = CommandUtil.readDomain(is);
        } else if (addressType == AddrTypeEnum.IPV6.getCode()) {
            addr = CommandUtil.readIpv6(is);
        }
        int c1 = is.read();
        int c2 = is.read();
        int dstPort = ((c1 << 8) + c2) & 0xFFFF;
        this.version = (byte) version;
        this.cmd = (byte) cmd;
        this.rsv = (byte) rsv;
        this.addressType = (byte) addressType;
        this.dstAddr = addr;
        this.dstPort = dstPort;
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

    public int getDstPort() {
        return dstPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }
}
