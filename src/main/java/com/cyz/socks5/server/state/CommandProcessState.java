package com.cyz.socks5.server.state;

import com.cyz.socks5.server.config.ServerConfig;
import com.cyz.socks5.server.enums.*;
import com.cyz.socks5.server.error.ProxyServerException;
import com.cyz.socks5.server.HostResolver;
import com.cyz.socks5.server.message.CommandRequest;
import com.cyz.socks5.server.message.CommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;

/**
 * 认证完成，开始正常的socks命令
 */
public class CommandProcessState implements ProxyState{
    private final Logger logger = LoggerFactory.getLogger(CommandProcessState.class);

    private ServerConfig serverConfig;
    private Socket socket;

    private HostResolver hostResolver;

    public CommandProcessState(ServerConfig serverConfig, Socket socket) {
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
            logger.error("Command process failed, {}", pse.getMessage());
            //状态切换
            return this;
        }
    }

    private ProxyState handleCmd(CommandRequest request) throws IOException {
        byte opcode = request.getCmd();
        CommandTypeEnum cmd = CommandTypeEnum.fromCode(opcode);
        logger.info("receiving command "+cmd);
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
                return onUnknownCmd();
        }
    }

    private ProxyState onUnknownCmd() throws IOException{
        CommandResponse cmdResponse = new CommandResponse();
        cmdResponse.setVersion((byte)0x05);
        cmdResponse.setResponse((byte)CommandResponseEnum.UNKNOWN_CMD.getCode());
        cmdResponse.serialize(this.socket.getOutputStream());
        return this;
    }

    private ProxyState onConnect(CommandRequest request) throws IOException{
        //与目标服务器建立tcp连接
        String host = null;
        try{
            host = this.hostResolver.resolveHost(request.getAddressType(), request.getDstAddr());
        }
        catch (ProxyServerException pse){
            if(pse.getErrorEnum() == ClientErrorEnum.InvalidAddressType){
                return onBadAddressType();
            }
        }
        if(host == null){
            return onBadHost();
        }
        int port = request.getDstPort();
        Socket tgtSocket = null;
        try{
            logger.info("making connection to {}:{}", host, port);
            tgtSocket = new Socket(host, port);
        }
        catch (IOException ex){
            return onConnectionFailed(ex);
        }
        logger.info("Connection established to {}:{}", host, port);
        //回复客户端成功
        CommandResponse cmdResponse = new CommandResponse();
        cmdResponse.setVersion((byte)0x05);
        cmdResponse.setResponse((byte)CommandResponseEnum.SUCCESS.getCode());
        cmdResponse.setAddressType((byte)AddrTypeEnum.IPV4.getCode());
        cmdResponse.setBndAddr(this.hostResolver.hostToBytes(AddrTypeEnum.IPV4.getCode(), this.serverConfig.getIp()));
        cmdResponse.setBndPort(this.serverConfig.getPort());
        cmdResponse.serialize(this.socket.getOutputStream());
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


    private ProxyState onBadAddressType() throws IOException{
        CommandResponse response = new CommandResponse();
        response.setResponse((byte)CommandResponseEnum.UNKNOWN_ADDRTYPE.getCode());
        response.serialize(this.socket.getOutputStream());
        return this;
    }

    private ProxyState onBadHost() throws IOException {
        CommandResponse response = new CommandResponse();
        response.setResponse((byte)CommandResponseEnum.INVALID_HOST.getCode());
        response.serialize(this.socket.getOutputStream());
        return this;
    }

    private ProxyState onConnectionFailed(IOException ex) throws IOException {
        String message = ex.getMessage().toLowerCase();
        logger.warn(message);
        CommandResponseEnum error = null;
        if (ex instanceof ConnectException && message.contains("refuse")) {
            error = CommandResponseEnum.REFUSED;
        }
        else{
            //例如：
            //SocketException: connection timed out: connect
            error = CommandResponseEnum.BAD_NETWORK;
        }
        CommandResponse response = new CommandResponse();
        response.setResponse((byte) error.getCode());
        response.serialize(this.socket.getOutputStream());
        return this;
    }

    @Override
    public ServerStatusEnum getStatus() {
        return ServerStatusEnum.CommandProcess;
    }
}
