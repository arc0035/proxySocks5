package com.cyz.socks5.niodemo;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioServer2 {
    private Selector selector;
    private int port = 8000;
    private ServerSocketChannel serverSocketChannel;
    private ExecutorService executorService;

    public static final int POOL = 4;

    public NioServer2() throws IOException {
        this.selector = Selector.open();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().setReuseAddress(true);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));


    }

    public void service() throws IOException{
        serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0){
            Set readyKeys = selector.selectedKeys();
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey key = null;
                try{
                    key = (SelectionKey) iterator.next();
                    iterator.remove();
                    if(key.isAcceptable()){handleAccept(key);}
                    if(key.isReadable()){}
                    if(key.isWritable()){}//什么情况下是可写的？
                    if(key.isConnectable()){}
                }
                catch (Exception ex){
                    ex.printStackTrace();
                    if(key != null){
                        //取消监视
                        key.cancel();
                        key.channel().close();
                    }
                }
            }

        }
    }

    private void handleAccept(SelectionKey key) throws IOException{
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = (SocketChannel)ssc.accept();
        System.out.println("收到连接"+socketChannel.getRemoteAddress() );
        socketChannel.configureBlocking(false);
        //阻塞IO的意思就是调用IO不会阻塞
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        socketChannel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE, byteBuffer);
    }

    public void handleRead(SelectionKey selectionKey) throws IOException{
        ByteBuffer byteBuffer = (ByteBuffer)selectionKey.attachment();
        SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(32);
        int n = socketChannel.read(readBuffer);
        readBuffer.flip();
        byteBuffer.limit(byteBuffer.capacity());
        byteBuffer.put(readBuffer);
    }

    //写事件什么时候会发生呢？？？
    public void handleWrite(SelectionKey selectionKey) throws IOException{
        ByteBuffer byteBuffer = (ByteBuffer)selectionKey.attachment();
        SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
        byteBuffer.flip();
        String data = decode(byteBuffer);
        if(data.indexOf( "\n") == -1){
            return;
        }
        //....
    }

    private String decode(ByteBuffer buffer){
        return new String(buffer.array(), Charset.forName("GBK"));
    }

    private ByteBuffer encode(String output){
        return ByteBuffer.wrap(output.getBytes());
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
