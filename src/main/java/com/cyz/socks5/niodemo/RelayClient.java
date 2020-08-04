package com.cyz.socks5.niodemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;

public class RelayClient {

    public static void main(String[] args) throws IOException{

        Socket s = new Socket("127.0.0.1", 8000);



        //System.out.println(new BufferedReader(new InputStreamReader(s.getInputStream())).readLine());
        while (true){
            s.getOutputStream().write("Hello nio\n".getBytes());
            byte[] bytes = new byte[8192];
            int n = s.getInputStream().read(bytes);
            System.out.println(new String(Arrays.copyOfRange(bytes, 0, n)));
            System.in.read();
        }

    }

}
