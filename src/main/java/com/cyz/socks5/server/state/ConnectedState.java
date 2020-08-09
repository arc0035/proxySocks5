package com.cyz.socks5.server.state;

import com.cyz.socks5.server.TcpRelayingTask;
import com.cyz.socks5.server.enums.ServerStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ConnectedState implements ProxyState {
    private static final Logger logger = LoggerFactory.getLogger(ConnectedState.class);

    /**
     * 这个时候，该线程同时管理两个socket，一个客户端，一个远程代理。如果该线程读某个客户端的数据陷入阻塞，就无法处理另一个socket的IO请求。
     * 解决办法，要么继续分出一个线程，要么IO多路复用，这里使用IO多路复用，优点是单线程处理数据，简单。
     */
    public ConnectedState(SocketChannel srcChannel, SocketChannel tgtChannel)  throws IOException{
        TcpRelayingTask.register(srcChannel, tgtChannel);
    }
    @Override
    public ProxyState next() throws IOException {
        return new RelayingState();
    }

    @Override
    public ServerStatusEnum getStatus() {
        return ServerStatusEnum.Connected;
    }
}
