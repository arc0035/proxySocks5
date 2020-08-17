package com.cyz.socks5.client;

import com.cyz.socks5.server.HostResolver;
import com.cyz.socks5.server.enums.AddrTypeEnum;
import com.cyz.socks5.server.enums.AuthenticationMethod;
import com.cyz.socks5.server.enums.CommandResponseEnum;
import com.cyz.socks5.server.enums.CommandTypeEnum;
import com.cyz.socks5.server.message.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

/**
 * 以下现象表示连接已无法IO，不一定关闭
 * java.net.SocketException: Software caused connection abort: socket write error
 * java.net.SocketException: connection reset by peer
 * IOException: 你的主机中的软件中止了一个已建立的连接
 * KeyCancellationException is throwned by isReadable(). This often caused by channel is closed and automatically deregistered
 * ClosedChannelException is throwned by common io operations like getRemoteAddress。而且即使仅仅调研shutdownOutput，连接处于连接、打开状态，也会抛出
 * SelectionKey.isValid() returns false
 * read() returns -1.(服务端close，或者shutdownOutput)
 *
 *
 */
public class Client {

    public static void main(String[] args) throws Exception {

        Socket socket = new Socket("127.0.0.1", 1080);
        testHandshake(socket);
        Thread.sleep(1000);
        testAuthenticate(socket);
        Thread.sleep(1000);
        CommandResponse response = testCmd(socket, CommandTypeEnum.UDP, "127.0.0.1",8000);
      //  testCmd(socket, CommandTypeEnum.CONNECT,"www.baidu.com",80);
        //testTcpChat(socket);
        testUdpChat(new InetSocketAddress(new HostResolver().resolveHost(response.getAddressType(), response.getBndAddr()), response.getBndPort()));
    }

    private static void testTcpChat(Socket socket) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            String line = reader.readLine();
            if("bye".compareToIgnoreCase(line) == 0){
                socket.close();
                return;
            }
            line+="\n";
            socket.getOutputStream().write(line.getBytes());
            byte[] bytes = new byte[1024];
            int n = socket.getInputStream().read(bytes);
            if(n <= 0){
                System.out.println("读到："+n);
            }
            else{
                System.out.println("[服务端]:"+new String(Arrays.copyOfRange(bytes, 0, n)));
            }

        }
    }

    private static void testUdpChat(SocketAddress relay) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        DatagramChannel udpChannel = DatagramChannel.open();
        udpChannel.connect(relay);
        while(true){
            String line = reader.readLine();
            if("bye".compareToIgnoreCase(line) == 0){
                return;
            }
            udpChannel.write(ByteBuffer.wrap(line.getBytes()));
            /*
            ByteBuffer bb = ByteBuffer.allocate(1024);
            int n = udpChannel.read(bb);
            if(n <= 0){
                System.out.println("读到："+n);
            }
            else{
                System.out.println("[服务端]:"+new String(Arrays.copyOfRange(bb.array(), 0, n)));
            }

             */

        }
    }

    private static void testReplay(Socket socket) throws Exception {
        Thread.sleep(3000);
        byte[] bytes = "Hello nio\n".getBytes();
        socket.getOutputStream().write(bytes);
        System.out.println("发送完毕");
        bytes = new byte[1024];
        int n = socket.getInputStream().read(bytes);
        if(n <= 0){
            System.out.println("返回数据:"+n);
        }
        else{
            System.out.println(new String(Arrays.copyOfRange(bytes, 0, n)));
        }

    }

    private static void testHandshake(Socket socket) throws IOException {
        System.out.println("发送握手请求");
        HandshakeRequest handshakeRequest = new HandshakeRequest();
        handshakeRequest.setVersion((byte)0x05);
        handshakeRequest.setMethodCount((byte)2);
        handshakeRequest.setMethods(new byte[] {
                (byte) AuthenticationMethod.NONE.getCode(),
                (byte)AuthenticationMethod.USERPASSWORD.getCode()});
        handshakeRequest.serialize(socket.getOutputStream());


        HandshakeResponse response = new HandshakeResponse();
        response.deserialize(socket.getInputStream());
        System.out.println("握手结果:"+response.getMethod());
    }

    private static void testAuthenticate(Socket socket) throws IOException {
        System.out.println("发送认证请求");
        String username = "zhangsan";
        String pwd = "123456";
        UserPasswordAuthenticationRequest request = new UserPasswordAuthenticationRequest();
        request.setVersion((byte)0x01);
        request.setUsername(username);
        request.setUsernameLength((byte)username.getBytes().length);
        request.setPassword(pwd);
        request.setPasswordLength((byte)pwd.getBytes().length);

        request.serialize(socket.getOutputStream());
        //socket.getOutputStream().flush();
        System.out.println("Sending msg complete");

        AuthenticationResultResponse result = new AuthenticationResultResponse();
        result.deserialize(socket.getInputStream());
        System.out.println(result.getResult()==0x00?"认证成功":"认证失败");
    }


    private static CommandResponse testCmd(Socket proxy, CommandTypeEnum cmd, String tgtHost, int tgtPort) throws IOException {
        System.out.println("发送命令");
        CommandRequest request = new CommandRequest();
        request.setCmd((byte)cmd.getCode());
        request.setAddressType((byte)AddrTypeEnum.DOMAIN.getCode());
        request.setDstAddr(new HostResolver().hostToBytes(AddrTypeEnum.DOMAIN.getCode(), tgtHost));
        //request.setDstAddr(new HostResolver().hostToBytes(AddrTypeEnum.DOMAIN.getCode(), "www.baiduahefa.com"));
        request.setDstPort(tgtPort);
        //request.setDstPort(777);
        request.serialize(proxy.getOutputStream());
        System.out.println("Sending cmd msg complete");

        CommandResponse result = new CommandResponse();
        result.deserialize(proxy.getInputStream());
        System.out.println(CommandResponseEnum.fromCode(result.getResponse()).name());
        System.out.println(result.getBndPort());
        return result;
    }

}
