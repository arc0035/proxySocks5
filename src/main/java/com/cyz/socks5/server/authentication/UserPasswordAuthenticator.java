package com.cyz.socks5.server.authentication;

import com.cyz.socks5.server.enums.AuthenticationMethod;
import com.cyz.socks5.server.message.UserPasswordAuthenticationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.*;

public class UserPasswordAuthenticator implements Authenticator {
    private final String USERS = "users.properties";
    private final Logger logger = LoggerFactory.getLogger(UserPasswordAuthenticator.class);

    private Map<String, String> userRealm = new HashMap<>();
    public UserPasswordAuthenticator(){
        init();
    }

    private void init(){
        //加载用户名密码配置
        try(InputStream is = UserPasswordAuthenticator.class.getClassLoader().getResourceAsStream(USERS)){
            Properties properties = new Properties();
            properties.load(is);
            this.userRealm = new HashMap<>();
            for(String pname:properties.stringPropertyNames()){
                this.userRealm.put(pname, properties.getProperty(pname));
            }
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isAuthenticated(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        UserPasswordAuthenticationRequest request = new UserPasswordAuthenticationRequest();
        request.deserialize(is);

        String username = request.getUsername();
        String password = request.getPassword();

        return pwdMatch(this.userRealm.get(username), password);
    }

    protected boolean pwdMatch(String expected, String actual){
        return Objects.equals(expected, actual);
    }

    @Override
    public AuthenticationMethod getAuthenticationMethod() {
        return AuthenticationMethod.USERPASSWORD;
    }
}
