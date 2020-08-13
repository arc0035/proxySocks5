package com.cyz.socks5.niodemo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.Scanner;

public class UdpClient {

    /**
     * UDP和TCP比，不需要创建连接，直接发送数据即可
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        DatagramChannel channel= DatagramChannel.open();

        String newData="hello,itbuluoge!"+System.currentTimeMillis();
        ByteBuffer buf= ByteBuffer.allocate(48);
        buf.clear();
        buf.put(newData.getBytes());
        buf.flip();
        /*发送UDP数据包*/
        int bytesSent=channel.send(buf, new InetSocketAddress("127.0.0.1",6666));
        System.in.read();
    }
}
