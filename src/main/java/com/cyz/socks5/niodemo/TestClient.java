package com.cyz.socks5.niodemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TestClient {

    public static void main(String[] args) throws IOException{

        Socket s = new Socket("127.0.0.1", 8000);

        s.getOutputStream().write("Hello nio\n".getBytes());

        System.out.println(new BufferedReader(new InputStreamReader(s.getInputStream())).readLine());
        System.in.read();
    }

}
