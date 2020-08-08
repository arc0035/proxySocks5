package com.cyz.socks5.niodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author aaronchu
 * @Description
 * @data 2020/08/08
 */
public class EchoServer {

    public static void main(String[] args) throws Exception{
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8000));
        ssc.socket().setReuseAddress(true);
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("聊天服务器已启动");
        while (true){
            int n = selector.select();
            if(n <= 0){
                continue;
            }
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()){
                SelectionKey key = it.next();
                try{
                    if(key.isAcceptable()){
                        handleAccept(key, selector);
                    }
                    if(key.isReadable()){
                        handleReadable(key);
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }

                it.remove();
            }
        }


    }

    private static void handleReadable(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer bb = ByteBuffer.allocate(1024);
        try{
            int n = channel.read(bb);
            if(n == -1){
                System.out.println("关闭连接");
                channel.close();
                return;
            }
            System.out.println("[客户端]:"+new String(Arrays.copyOfRange(bb.array(), 0, n)));
            bb.flip();
            channel.write(bb);
            bb.clear();
        }
        catch (IOException ex){
            ex.printStackTrace();
            try{
                channel.close();
            }catch (Exception e){}

        }

    }

    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel channel = ssc.accept();
        channel.configureBlocking(false);
        System.out.println("连接已建立");
        channel.register(selector, SelectionKey.OP_READ);
    }

}
