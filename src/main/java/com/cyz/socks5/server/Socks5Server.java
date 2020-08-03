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
            log.warn("Server already started, cannot start again");
            return;
        }
        this.ssc = ServerSocketChannel.open();
        this.ssc.bind(
                new InetSocketAddress(
                    InetAddress.getByName(this.config.getIp()),
                    this.config.getPort()),
                this.config.getBackLog());
        this.executor = new ThreadPoolExecutor(this.config.getMaxClients(), this.config.getMaxClients(),
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), (r, executor) -> {
                    throw new RejectedExecutionException();
                });
        log.info("Server started, listening on {}", config.getPort());
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
        //Socket的IO永远是阻塞的。SocketChannel的IO可以是阻塞的，也可以是非阻塞的。
        //这里让认证采用阻塞IO，流量转发采用非阻塞IO。尽量保持已有架构不变。
        this.executor.execute(new SocketHandler(this.config, channel.socket()));
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
