package com.cyz.socks5.server.state;

import com.cyz.socks5.server.enums.ServerStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ConnectedState implements ProxyState {
    private static final Logger logger = LoggerFactory.getLogger(ConnectedState.class);
    private static final int BUF_SIZE = 4096;
    //1 能否让SocketChannel也注册到Selector中？使用Socket.getChannel方法
    //2 能否让Socket变成SocketChannel
    private SocketChannel channel1;
    private SocketChannel channel2;
    private ByteBuffer buf1;
    private ByteBuffer buf2;
    private Selector selector;

    public ConnectedState(SocketChannel srcChannel, SocketChannel tgtChannel)  throws IOException{
        this.channel1 = srcChannel;
        this.channel2 = tgtChannel;
        this.buf1 = ByteBuffer.allocate(BUF_SIZE);
        this.buf2 = ByteBuffer.allocate(BUF_SIZE);
        this.selector = Selector.open();
        init();
    }

    /**
     * 这个时候，该线程同时管理两个socket，一个客户端，一个远程代理。如果该线程读某个客户端的数据陷入阻塞，就无法处理另一个socket的IO请求。
     * 解决办法，要么继续分出一个线程，要么IO多路复用，这里使用IO多路复用，优点是单线程处理数据，简单。
     */
    private void init() throws IOException{
        //IO确保是非阻塞模式
        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);
        //把两个通道注册到选择器上，让选择器管理这两个通道
        channel1.register(this.selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
        channel2.register(this.selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
    }


    @Override
    public ProxyState next() throws IOException {
        //Do multiplexing. Keep polling on each socket to see whether data is ready, only when data ready fetch data
        int n;
        while ((n = this.selector.select()) >= 0) {
            if (n == 0) {
                //Can possibly returns zero
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = null;
                key = iterator.next();
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
        return this;
    }

    private void handleRead(SelectionKey key) throws IOException{
        //Read buffer from network
        System.out.println("HANDLING.read ");
        SocketChannel c = (SocketChannel) key.channel();
        ByteBuffer buffer = readBuffer(c);
        int n = c.read(buffer);
        System.out.println("DEBUG.actual read:  "+n + " from "+c.getRemoteAddress());

        handleWrite(key, buffer);
    }


    private void handleWrite(SelectionKey key, ByteBuffer buffer) throws IOException{
        //Write buffer to network
        System.out.println("HANDLING.write ");
        SocketChannel c = opChannel((SocketChannel) key.channel());
        buffer.flip();
        int n = 0;
        int total = buffer.limit();
        while (n < total){
            n += c.write(buffer);
        }
        System.out.println("DEBUG.actual write:  "+n + " to "+c.getRemoteAddress());
        buffer.clear();
    }

    private SocketChannel opChannel(SocketChannel c) {
        if(this.channel1 == c){
            return this.channel2;
        }
        if(this.channel2 == c){
            return this.channel1;
        }
        //This really should not happen
        throw new RuntimeException("Internal Error");
    }


    private ByteBuffer readBuffer(SocketChannel c) {
        if(this.channel1 == c){
            return this.buf1;
        }
        if(this.channel2 == c){
            return this.buf2;
        }
        //This really should not happen
        throw new RuntimeException("Internal Error");
    }

    private ByteBuffer writeBuffer(SocketChannel c) {
        if(this.channel1 == c){
            return this.buf2;
        }
        if(this.channel2 == c){
            return this.buf1;
        }
        //This really should not happen
        throw new RuntimeException("Internal Error");
    }

    @Override
    public ServerStatusEnum getStatus() {
        return ServerStatusEnum.Connected;
    }
}
