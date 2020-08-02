package com.cyz.socks5.server;

import com.cyz.socks5.server.config.ServerConfig;
import com.cyz.socks5.server.enums.ServerStatusEnum;
import com.cyz.socks5.server.state.DisconnectedState;
import com.cyz.socks5.server.state.InitState;
import com.cyz.socks5.server.state.ProxyState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * 专门负责和一个客户端的代理任务
 */
public class SocketHandler implements Runnable, Closeable {

    final private static Logger log = LoggerFactory.getLogger(SocketHandler.class);

    private ServerConfig serverConfig;
    private Socket socket;

    private ServerStatusEnum status;

    public SocketHandler(ServerConfig serverConfig, Socket socket){
        this.serverConfig = serverConfig;
        this.socket = socket;
        this.status = ServerStatusEnum.Init;
    }


    @Override
    public void run() {
        log.info("线程{}负责处理任务, 服务端口号:{}", Thread.currentThread().getName(), this.socket.getPort());
        ProxyState state = new InitState(this.serverConfig, this.socket);
        while (state != null  && state != DisconnectedState.INSTANCE) {
            try{
                state = state.next();
            }
            catch (Exception io){
                state = DisconnectedState.INSTANCE;
            }
        }
    }


    @Override
    public void close() throws IOException {
        if(socket != null){
            socket.close();;
        }
    }
}
