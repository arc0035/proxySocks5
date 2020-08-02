package com.cyz.socks5.server;

import com.cyz.socks5.server.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.*;

public class Socks5Server implements Closeable {

    final private static Logger log = LoggerFactory.getLogger(Socks5Server.class);

    private ServerConfig config;
    private ServerSocket serverSocket;
    private ThreadPoolExecutor executor;


    public Socks5Server(ServerConfig config) {
        this.config = config;
    }

    public synchronized void start() throws IOException {
        if(serverSocket != null){
            log.warn("Server already started, cannot start again");
        }
        this.serverSocket = new ServerSocket(this.config.getPort(), this.config.getBackLog());
        this.executor = new ThreadPoolExecutor(this.config.getMaxClients(), this.config.getMaxClients(),
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), (r, executor) -> {
                    throw new RejectedExecutionException();
                });
        log.info("Server started, listening on {}", serverSocket.getLocalPort());
        while (true){
            try{
                Socket socket = this.serverSocket.accept();
                log.info("New connection from client {} ", socket.getInetAddress().getHostAddress());
                service(socket);
            }
            catch (Exception ex){
                //防止打断循环
                log.error("",ex);
            }

        }
    }

    private void service(Socket socket) throws IOException{
        this.executor.execute(new SocketHandler(this.config, socket));
    }


    @Override
    public void close() throws IOException {
        if(this.serverSocket != null){
            this.serverSocket.close();
        }
    }
    /**
     * 启动代理服务器
     * @param args
     */
    public static void main(String[] args) throws Exception{
        //加载配置
        ServerConfig config = loadConfig();
        //启动服务器
        Socks5Server socks5Server = new Socks5Server(config);
        socks5Server.start();
    }

    private static ServerConfig loadConfig() throws IOException{
        ServerConfig config = new ServerConfig();
        config.load();
        return config;
    }

}
