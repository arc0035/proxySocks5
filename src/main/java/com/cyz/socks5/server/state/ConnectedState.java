package com.cyz.socks5.server.state;

import com.cyz.socks5.server.enums.ServerStatusEnum;

import java.io.IOException;
import java.net.Socket;

public class ConnectedState implements ProxyState {

    private Socket srcSocket;
    private Socket tgtSocket;

    public ConnectedState(Socket srcSocket, Socket tgtSocket) {
        this.srcSocket = srcSocket;
        this.tgtSocket = tgtSocket;
    }

    @Override
    public ProxyState next() throws IOException {
        return null;
    }

    @Override
    public ServerStatusEnum getStatus() {
        return ServerStatusEnum.Connected;
    }
}
