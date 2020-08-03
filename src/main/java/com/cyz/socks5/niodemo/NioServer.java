package com.cyz.socks5.niodemo;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioServer {
    private int port = 8000;
    private ServerSocketChannel serverSocketChannel;
    private ExecutorService executorService;

    public static final int POOL = 4;

    public NioServer() throws IOException{
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().setReuseAddress(true);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));


    }

    public void service() {
        while (true){
            SocketChannel channel = null;
            try{
                channel = serverSocketChannel.accept();
                executorService.execute(new Handler(channel));
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception{
        new NioServer().service();
    }

    class Handler implements Runnable{

        private SocketChannel socketChannel;

        public Handler(SocketChannel channel) {
            this.socketChannel = channel;
        }

        @Override
        public void run() {
            Socket socket = socketChannel.socket();
            try {
                System.out.println("收到客户连接" + socket.getInetAddress() + " :" + socket.getPort());
                //思考：BufferedReader怎么实现的，如何解决网络流的堵塞问题？
                BufferedReader reader = getReader(socket);
                BufferedWriter writer = getWriter(socket);
                String msg = null;
                while ((msg = reader.readLine()) != null) {
                    System.out.println("client:" + msg);
                    if (msg.equals("bye")) {
                        break;
                    }
                    writer.append(echo(msg));
                    writer.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    this.socketChannel.close();
                } catch (Exception ex) {
                }
            }
        }

        private BufferedWriter getWriter(Socket socket) throws IOException{
            return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        private BufferedReader getReader(Socket socket) throws IOException{
            return new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public String echo(String msg){
            return "echo:"+msg+"\n";
        }


    }

    
}
