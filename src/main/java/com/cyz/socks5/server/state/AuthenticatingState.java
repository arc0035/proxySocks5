package com.cyz.socks5.server.state;

import com.cyz.socks5.server.authentication.Authenticator;
import com.cyz.socks5.server.authentication.Authenticators;
import com.cyz.socks5.server.config.ServerConfig;
import com.cyz.socks5.server.enums.ServerStatusEnum;
import com.cyz.socks5.server.enums.ClientErrorEnum;
import com.cyz.socks5.server.error.ProxyServerException;
import com.cyz.socks5.server.message.AuthenticationResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class AuthenticatingState implements ProxyState {
    private final Logger logger = LoggerFactory.getLogger(AuthenticatingState.class);

    private ServerConfig serverConfig;
    private Socket socket;

    public AuthenticatingState(ServerConfig serverConfig, Socket socket){
        this.serverConfig = serverConfig;
        this.socket = socket;
    }


    @Override
    public ProxyState next() throws IOException {
        try{
            Authenticator authenticator = Authenticators.getAuthenticator(this.serverConfig.getAuthenticationMethod());
            //执行认证逻辑
            boolean isAuthenticated = authenticator.isAuthenticated(this.socket);
            //回复认证结果
            AuthenticationResultResponse result = new AuthenticationResultResponse();
            result.setVersion((byte)0x01);
            result.setResult(isAuthenticated?AuthenticationResultResponse.SUCCESS:0x01);
            result.serialize(this.socket.getOutputStream());
            if(isAuthenticated){
                logger.info("Authentication succeeded, waiting for command");
                return new WaitingForCommandState(this.serverConfig, this.socket);
            }
            throw new ProxyServerException(ClientErrorEnum.AuthenticationFailed);
        }
        catch (ProxyServerException pse){
            logger.warn("Authentication failed, switch to basic status :{}", pse.getMessage());
            //状态切换
            return new InitState(this.serverConfig, this.socket);
        }
        //连接层的错误由外部大循环兜底【领悟：错误处理的关键在于把错误分层】
    }

    @Override
    public ServerStatusEnum getStatus() {
        return ServerStatusEnum.Authenticating;
    }
}
