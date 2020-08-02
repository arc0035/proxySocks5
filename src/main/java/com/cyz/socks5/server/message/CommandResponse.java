package com.cyz.socks5.server.message;

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

    }

    @Override
    public void deserialize(InputStream is) throws IOException {

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

