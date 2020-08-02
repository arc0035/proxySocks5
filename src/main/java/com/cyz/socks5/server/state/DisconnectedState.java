package com.cyz.socks5.server.state;

import com.cyz.socks5.server.enums.ServerStatusEnum;

import java.io.IOException;

public class DisconnectedState implements ProxyState {

    public static DisconnectedState INSTANCE = new DisconnectedState();

    private DisconnectedState(){}

    @Override
    public ProxyState next() throws IOException {
        return null;
    }

    @Override
    public ServerStatusEnum getStatus() {
        return ServerStatusEnum.Disconnected;
    }
}
