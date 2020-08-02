package com.cyz.socks5.server.state;

import com.cyz.socks5.server.config.ServerConfig;
import com.cyz.socks5.server.enums.ClientErrorEnum;
import com.cyz.socks5.server.enums.CommandEnum;
import com.cyz.socks5.server.enums.ServerStatusEnum;
import com.cyz.socks5.server.error.ProxyServerException;
import com.cyz.socks5.server.HostResolver;
import com.cyz.socks5.server.message.CommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * 认证完成，开始正常的socks命令
 */
public class WaitingForCommandState implements ProxyState{
    private final Logger logger = LoggerFactory.getLogger(WaitingForCommandState.class);

    private ServerConfig serverConfig;
    private Socket socket;

    private HostResolver hostResolver;

    public WaitingForCommandState(ServerConfig serverConfig, Socket socket) {
        this.serverConfig = serverConfig;
        this.socket = socket;
        this.hostResolver = new HostResolver();
    }

    @Override
    public ProxyState next() throws IOException {
        try{
            InputStream is = this.socket.getInputStream();
            CommandRequest request = new CommandRequest();
            request.deserialize(is);

            return handleCmd(request);
        }
        catch (ProxyServerException pse){
            logger.warn("Authentication failed, switch to basic status :{}", pse.getMessage());
            //状态切换
            return new InitState(this.serverConfig, this.socket);
        }
    }

    private ProxyState handleCmd(CommandRequest request) {
        byte opcode = request.getCmd();
        CommandEnum cmd = CommandEnum.fromCode(opcode);
        switch (cmd){
            case CONNECT:{
                return onConnect(request);
            }
            case BIND:{
                return onBind(request);
            }
            case UDP:{
                return onUdp(request);
            }
            default:
                throw new ProxyServerException(ClientErrorEnum.InvalidCmdOpcode);
        }
    }

    private ProxyState onConnect(CommandRequest request){
        //与目标服务器建立tcp连接
        String host = this.hostResolver.resolveHost(request.getAddressType(), request.getDstAddr());
        int port = request.getDstPort();
        Socket tgtSocket = null;
        try{
            tgtSocket = new Socket(host, port);
        }
        catch (IOException ex){

        }
        //回复客户端结果

        //切换到connected状态
        return new ConnectedState(this.socket, tgtSocket);
    }

    private ProxyState onBind(CommandRequest request){
        //如果当前已经是connected状态，就监听某一端口
        //问题：怎么用
        throw new ProxyServerException(ClientErrorEnum.InvalidCmdOpcode);
    }

    private ProxyState onUdp(CommandRequest request){
        throw new ProxyServerException(ClientErrorEnum.InvalidCmdOpcode);
    }


    @Override
    public ServerStatusEnum getStatus() {
        return ServerStatusEnum.WaitingForCommand;
    }
}
