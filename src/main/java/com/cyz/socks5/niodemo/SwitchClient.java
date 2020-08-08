package com.cyz.socks5.niodemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * 以下现象表示连接已被关闭：
 * java.net.SocketException: Software caused connection abort: socket write error
 * java.net.SocketException: connection reset by peer
 * KeyCancellationException
 * SelectionKey.isValid() returns false
 * read() returns -1.(服务端shutdown)
 *
 *
 */
public class SwitchClient {

    public static void main(String[] args) throws Exception{
        /*
        SocketChannel s = SocketChannel.open();
        s.configureBlocking(false);
        Selector selector = Selector.open();
        s.register(selector, SelectionKey.OP_CONNECT);
        System.out.println(s.isOpen() + " " +s.isConnected() + " "+s.isConnectionPending());
        int n = selector.select();
        System.out.println("Connectable");
        System.in.read();

         */

        Socket s = new Socket("127.0.0.1",8000);
        System.out.println(s.isConnected());
        handshake(s);
        System.out.println("握手完毕，开始聊天");
        communicate(s);
        System.in.read();
    }

    private static void handshake(Socket s) throws IOException {
        byte[] ping = "ping".getBytes();
        s.getOutputStream().write(ping.length);
        s.getOutputStream().write(ping);
        int len = s.getInputStream().read();
        byte[] pong = new byte[len];
        s.getInputStream().read(pong);

        System.out.println(new String(pong));
    }

    private static void communicate(Socket s) throws IOException {
        while (true) {
            try {
                Thread.sleep(3000);
            } catch (Exception ex) {
            }
            byte[] ping = "hello".getBytes();
            s.getOutputStream().write(ping.length);
            s.getOutputStream().write(ping);
            int len = s.getInputStream().read();
            if(len >= 0){
                byte[] pong = new byte[len];
                s.getInputStream().read(pong);

                System.out.println(new String(pong));
            }
            else{
                System.out.println("读到-1");
            }

        }
    }


}
