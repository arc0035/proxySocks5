package com.cyz.socks5.niodemo;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class WritableChannelTest {

    public static void main(String[] args) throws Exception{
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8000), 111);
        ssc.socket().setReuseAddress(true);
        System.out.println("服务器已建立");
        SocketChannel channel = ssc.accept();
        channel.configureBlocking(false);
        System.out.println("通道已建立...");
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_WRITE|SelectionKey.OP_READ);
        try{
            int nselect;
            while ((nselect = selector.select()) >= 0){
                if(nselect > 0){
                    SelectionKey key = selector.selectedKeys().iterator().next();

                    if(key.isReadable()){
                        ByteBuffer bb = ByteBuffer.allocate(4096);
                        channel.read(bb);
                        continue;
                    }
                    if(key.isWritable()){
                        System.out.println("Hi");
                    }
                }

            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
