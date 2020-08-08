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
import java.net.SocketException;

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
        while (state != null  && state != DisconnectedState.INSTANCE && state.getStatus() != ServerStatusEnum.Relaying) {
            try{
                state = state.next();
                log.info("new state:{}",state.getStatus().name());
            }
            catch (SocketException socketEx){
                //例如调用read()读取数据时，客户端已经断开连接，就会报Socket exception: connection reset
                log.warn("socket exception, so close the thread", socketEx);
                state = DisconnectedState.INSTANCE;
            }
            catch (IOException io){
                //例如远程客户端关闭了连接
                log.warn("io exception, so close the thread", io);
                state = DisconnectedState.INSTANCE;
            }
            catch (Exception ex){
                //如果抛出异常，应该属于编程失误
                log.error("Sever exception ",ex);
                state = DisconnectedState.INSTANCE;
            }
        }

        try{
            this.close();
        }
        catch (Exception ex){}

        log.info("退出线程 {}",Thread.currentThread().getName());
    }


    @Override
    public void close() throws IOException {
        //可以通过soLinger选项指定socket在close时是否把未发送干净的数据都发送干净
        /*
        if(socket != null){
            socket.close();;
        }

         */
    }
}
