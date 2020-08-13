package com.cyz.socks5.niodemo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpServer {

    public static void main(String[] args) throws Exception{
        //Udp 实践

        DatagramChannel channel=DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(6666));

        while(true){
            ByteBuffer buf=ByteBuffer.allocate(48);
            buf.clear();
            /*阻塞，等待发来的数据*/
            InetSocketAddress remote = (InetSocketAddress) channel.receive(buf);
            /*设置缓冲区可读*/
            buf.flip();
            System.out.println(remote);
            /*循环读出所有字符*/
            while(buf.hasRemaining()) {

                System.out.print((char) buf.get());
            }
            System.out.println();
        }

    }
}
