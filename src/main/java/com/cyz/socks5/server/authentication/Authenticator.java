package com.cyz.socks5.server.authentication;

import com.cyz.socks5.server.enums.AuthenticationMethod;

import java.io.IOException;
import java.net.Socket;

public interface Authenticator {

    /**
     * 执行具体的认证逻辑
     * @return
     */
    boolean isAuthenticated(Socket socket) throws IOException;

    /**
     * 该认证器所支持的方法
     * @return
     */
    AuthenticationMethod getAuthenticationMethod();

}
