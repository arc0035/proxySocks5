package com.cyz.socks5.server.state;

import com.cyz.socks5.server.config.ServerConfig;
import com.cyz.socks5.server.enums.ServerStatusEnum;
import com.cyz.socks5.server.enums.CommonErrorEnum;
import com.cyz.socks5.server.error.ProxyServerException;
import com.cyz.socks5.server.message.HandshakeRequest;
import com.cyz.socks5.server.message.HandshakeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 初始状态
 */
public class InitState implements ProxyState {
    private final Logger logger = LoggerFactory.getLogger(InitState.class);

    private ServerConfig serverConfig;
    private Socket socket;

    public InitState(ServerConfig serverConfig, Socket socket){
        this.serverConfig = serverConfig;
        this.socket = socket;
    }


    @Override
    public ProxyState next() throws IOException {
        try{
            InputStream is = this.socket.getInputStream();
            OutputStream os = this.socket.getOutputStream();
            //读取客户端支持的认证方案
            HandshakeRequest request = new HandshakeRequest();
            request.deserialize(is);
            verify(request);
            boolean support = false;
            byte[] methods = request.getMethods();
            //选择支持的认证方案
            for(int i=0;i<methods.length;i++){
                if(methods[i] == (byte)this.serverConfig.getAuthenticationMethod().getCode()){
                    support = true;
                    break;
                }
            }
            if(!support){
                throw new ProxyServerException(CommonErrorEnum.NoSupportAuthenticationMethod);
            }
            //回复客户
            HandshakeResponse response = new HandshakeResponse();
            response.setVersion((byte)0x05);
            response.setMethod((byte)this.serverConfig.getAuthenticationMethod().getCode());
            response.serialize(os);
            //切换状态
            return new AuthenticatingState(this.serverConfig, this.socket);
        }
        catch (ProxyServerException pse){
            logger.warn("bad request, switch to init status: {}",pse.getMessage());
            return this;
        }
        //连接层的错误由外部大循环兜底【领悟：错误处理的关键在于把错误分层】
    }

    private void verify(HandshakeRequest request){
        if(request.getVersion() != (byte)0x05){
            logger.warn("version "+request.getVersion());
            throw new ProxyServerException(CommonErrorEnum.InvalidVersion);
        }
        if(request.getMethodCount() <= 0){
            throw new ProxyServerException(CommonErrorEnum.BadAuthenticationMethodCount);
        }
    }

    @Override
    public ServerStatusEnum getStatus() {
        return ServerStatusEnum.Init;
    }
}
