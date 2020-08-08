package com.cyz.socks5.server;

import com.cyz.socks5.server.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.concurrent.*;

public class Socks5Server implements Closeable {

    final private static Logger log = LoggerFactory.getLogger(Socks5Server.class);

    private ServerConfig config;
    private ServerSocketChannel ssc;
    private ThreadPoolExecutor executor;


    public Socks5Server(ServerConfig config) {
        this.config = config;
    }

    public synchronized void start() throws IOException {
        if(ssc != null){
            log.warn("Socks5 proxy server already started, cannot start again");
            return;
        }
        this.ssc = ServerSocketChannel.open();
        this.ssc.bind(
                new InetSocketAddress(
                    InetAddress.getByName(this.config.getIp()),
                    this.config.getPort()),
                this.config.getBackLog());
        this.executor = new ThreadPoolExecutor(0, this.config.getMaxClients(),
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), (r, executor) -> {
                    throw new RejectedExecutionException();
                });
        RelayingTask.start();
        log.info("Socks5 proxy server started, listening on {}", config.getPort());
        while (true){
            try{
                //就用阻塞模式accept，没毛病
                SocketChannel channel = this.ssc.accept();
                log.info("New connection from client {} ", channel.socket().getInetAddress().getHostAddress());
                service(channel);
            }
            catch (Exception ex){
                //防止打断循环
                log.error("",ex);
            }

        }
    }

    private void service(SocketChannel channel) throws IOException{
        this.executor.execute(new SocketHandler(this.config,  channel.socket()));
    }


    @Override
    public void close() throws IOException {
        if(this.ssc != null){
            this.ssc.close();
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
