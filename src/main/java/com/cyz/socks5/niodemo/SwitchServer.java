package com.cyz.socks5.niodemo;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * 测试先使用BIO进行握手，然后再用NIO进行握手，是否存在连接断裂的现象。
 * Software caused connection abort: socket write error
 */
public class SwitchServer {

    public static void main(String[] arg) throws Exception{

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8000), 5);
        selector = Selector.open();
        while (true){
            SocketChannel channel = ssc.accept();
            bio(channel.socket());//通过SocketInputStream交互一条PING PONG
            nio(channel);//将Channel注册到Selector，然后等消息可读后，打印数据并返回。
        }
    }

    private static void bio(Socket socket) throws IOException{
        Utf8String ping  = new Utf8String();
        ping.deserialize(new DataInputStream(socket.getInputStream()));
        System.out.println("receive:"+ ping.getContent());
        Utf8String pong = new Utf8String();
        pong.setContent("pong");
        pong.serialize(new DataOutputStream(socket.getOutputStream()));
    }

    private static void nio(SocketChannel channel) throws IOException {
        new Thread(() -> {
            try {
                nioImpl(channel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private static Selector selector;
    private static void nioImpl(SocketChannel channel) throws IOException{
        System.out.println("切换到NIO状态");
        channel.configureBlocking(false);

        channel.register(selector, SelectionKey.OP_READ);

        while (true){
            int n = selector.select();
            if(n <= 0) continue;

            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            while (it.hasNext()){
                SelectionKey key = it.next();
                it.remove();
                //Handle event

                SocketChannel sc = (SocketChannel)key.channel();
                if(key.isReadable()){
                    System.out.println("可读");
                    ByteBuffer bb = ByteBuffer.allocate(1024);
                    sc.read(bb);
                    byte[] data = bb.array();
                    int actualLen = data[0];
                    byte[] actual = Arrays.copyOfRange(data, 1, 1+actualLen);
                    System.out.println("读取到"+new String(actual));

                    byte[] world = "world".getBytes();
                    byte[] all = new byte[world.length+1];
                    all[0] = (byte)world.length;
                    System.arraycopy(world, 0, all, 1, world.length);
                     bb = ByteBuffer.wrap(all);
                    bb.clear();
                    sc.write(bb);
                }
                /*
                if(key.isWritable()){
                    确实有一直可写的情况！
                }

                 */
            }
        }
    }

    public static class Utf8String{

        /**
         * Java typed string
         */
        private String content;

        public void setContent(String content){
            this.content = content;
        }

        public String getContent(){
            return this.content;
        }

        public void serialize(DataOutput dataOutput) throws IOException {
            dataOutput.write(content.length());
            dataOutput.write(content.getBytes());
        }

        public void deserialize(DataInput dataInput) throws IOException{
            int length = dataInput.readByte();
            byte[] bytes = new byte[length];
            dataInput.readFully(bytes);
            this.content = new String(bytes);
        }

    }

}
