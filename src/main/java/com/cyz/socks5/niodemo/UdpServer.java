package com.cyz.socks5.niodemo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpServer {

    public static void main(String[] args) throws Exception{
        //Udp 实践

        DatagramChannel channel=DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(6666));

        ByteBuffer buf=ByteBuffer.allocate(48);
        buf.clear();
        /*阻塞，等待发来的数据*/
        channel.receive(buf);
        /*设置缓冲区可读*/
        buf.flip();

        /*循环读出所有字符*/
        while(buf.hasRemaining()) {
            System.out.print((char) buf.get());
        }
    }
}
