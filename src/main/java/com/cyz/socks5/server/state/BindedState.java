package com.cyz.socks5.server.state;

import com.cyz.socks5.server.enums.ServerStatusEnum;

import java.io.IOException;

public class BindedState implements ProxyState {
    @Override
    public ProxyState next() throws IOException {
        return null;
    }

    @Override
    public ServerStatusEnum getStatus() {
        return ServerStatusEnum.Binded;
    }
}
