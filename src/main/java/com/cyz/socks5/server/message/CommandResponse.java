package com.cyz.socks5.server.message;

import com.cyz.socks5.server.enums.AddrTypeEnum;
import com.cyz.socks5.server.enums.CommandResponseEnum;
import com.cyz.socks5.server.util.CommandUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 命令响应。version||response||rsv||addressType||bind.addr||bind.port
 */
public class CommandResponse implements SocksMessage{

    private byte version = (byte)0x05;

    private byte response;

    private byte rsv;

    private byte addressType;

    private byte[] bndAddr;

    private int bndPort;

    @Override
    public void serialize(OutputStream os) throws IOException {
        os.write(version);
        os.write(response);
        os.write(rsv);
        if(response != CommandResponseEnum.SUCCESS.getCode()){
            return;
        }
        os.write(addressType);
        os.write(bndAddr);
        byte[] portBytes = new byte[2];
        portBytes[0] = (byte)(bndPort >> 8);
        portBytes[1] = (byte)bndPort;
        os.write(portBytes);
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        int version = is.read();
        int response = is.read();
        int rsv = is.read();
        this.version = (byte) version;
        this.response = (byte) response;
        this.rsv = (byte) rsv;
        if(response != CommandResponseEnum.SUCCESS.getCode()){
            return;
        }
        int addressType = is.read();
        byte[] addr = null;
        if (addressType == AddrTypeEnum.IPV4.getCode()) {
            addr = CommandUtil.readIpv4(is);
        } else if (addressType == AddrTypeEnum.DOMAIN.getCode()) {
            addr = CommandUtil.readDomain(is);
        } else if (addressType == AddrTypeEnum.IPV6.getCode()) {
            addr = CommandUtil.readIpv6(is);
        }
        int dstPort = ((is.read() << 8) + is.read()) & 0xFFFF;

        this.addressType = (byte) addressType;
        this.bndAddr = addr;
        this.bndPort = dstPort;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getResponse() {
        return response;
    }

    public void setResponse(byte response) {
        this.response = response;
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

    public byte[] getBndAddr() {
        return bndAddr;
    }

    public void setBndAddr(byte[] bndAddr) {
        this.bndAddr = bndAddr;
    }

    public int getBndPort() {
        return bndPort;
    }

    public void setBndPort(int bndPort) {
        this.bndPort = bndPort;
    }
}

