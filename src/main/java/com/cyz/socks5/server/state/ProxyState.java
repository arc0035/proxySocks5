package com.cyz.socks5.server.state;

import com.cyz.socks5.server.enums.ServerStatusEnum;

import java.io.IOException;

/**
 * 用状态机模式实现协议转换
 */
public interface ProxyState {

    ProxyState next() throws IOException;

    ServerStatusEnum getStatus();
}
