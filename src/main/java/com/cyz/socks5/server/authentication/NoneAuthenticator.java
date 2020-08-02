package com.cyz.socks5.server.authentication;

import com.cyz.socks5.server.enums.AuthenticationMethod;

import java.io.IOException;
import java.net.Socket;

public class NoneAuthenticator implements Authenticator {
    @Override
    public boolean isAuthenticated(Socket socket) throws IOException {
        return true;
    }

    @Override
    public AuthenticationMethod getAuthenticationMethod() {
        return AuthenticationMethod.NONE;
    }
}
