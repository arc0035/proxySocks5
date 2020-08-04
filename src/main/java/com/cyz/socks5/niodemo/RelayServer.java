package com.cyz.socks5.niodemo;

import com.cyz.socks5.server.RelayingTask;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class RelayServer implements Runnable{
    private static RelayingTask task ;
    public static void main(String[] args) throws Exception{
        RelayingTask.start();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8000), 111);
        ssc.socket().setReuseAddress(true);
        System.out.println("服务器已建立");

        while (true) {
            SocketChannel channel = ssc.accept();
            new Thread(new RelayServer().setClient(channel)).start();
        }
    }

    private SocketChannel client;
    @Override
    public void run() {
        try{
            SocketChannel baidu = SocketChannel.open();
            boolean connected = baidu.connect(new InetSocketAddress(InetAddress.getByName("www.baidu.com"),80));
            System.out.println("连接成功:"+connected);
            RelayingTask.register(client, baidu);
            System.out.println("退出线程!");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public RelayServer setClient(SocketChannel client) {
        this.client = client;
        return this;
    }
}
