package com.cyz.socks5.server.state;

import com.cyz.socks5.server.enums.ServerStatusEnum;

import java.io.IOException;

public class RelayingState implements ProxyState {

    @Override
    public ProxyState next() throws IOException {
        return this;
    }

    @Override
    public ServerStatusEnum getStatus() {
        return ServerStatusEnum.Relaying;
    }
}
