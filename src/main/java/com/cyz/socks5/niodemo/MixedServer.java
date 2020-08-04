package com.cyz.socks5.niodemo;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class MixedServer {

    public static void main(String[] args) throws Exception{
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8000), 111);
        ssc.socket().setReuseAddress(true);
        System.out.println("服务器已建立");
        SocketChannel channel = ssc.accept();
        channel.configureBlocking(false);
        System.out.println("开始读取...");
        while (true) {
            //int ndata = channel.read(ByteBuffer.allocate(1024));
            int ndata = channel.socket().getInputStream().read();
            System.out.println("读取字节:" + ndata);
            Thread.sleep(2000);
        }
    }
}
