package com.cyz.socks5.server.config;

import com.cyz.socks5.server.Socks5Server;
import com.cyz.socks5.server.enums.AuthenticationMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);

    /**
     * 代理服务器本地ip
     */
    private String ip;
    /**
     * 端口绑定
     */
    private int port;

    /**
     * 服务器连接队列数量
     */
    private int backLog;

    /**
     * 最大支持的同时连接的客户端数
     */
    private int maxClients;

    /**
     * 服务器端的认证方式
     */
    private AuthenticationMethod authenticationMethod;

    public ServerConfig() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBackLog() {
        return backLog;
    }

    public void setBackLog(int backLog) {
        this.backLog = backLog;
    }


    public int getMaxClients() {
        return maxClients;
    }

    public void setMaxClients(int maxClients) {
        this.maxClients = maxClients;
    }

    public AuthenticationMethod getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ip:");
        sb.append(this.ip);
        sb.append("\n");
        sb.append("port:");
        sb.append(this.port);
        sb.append("\n");
        sb.append("back log:");
        sb.append(this.backLog);
        sb.append("\n");
        sb.append("max clients:");
        sb.append(this.backLog);
        sb.append("\n");
        sb.append("authentication:");
        sb.append(this.authenticationMethod.getName());
        sb.append("\n");
        return sb.toString();
    }

    public void load() throws IOException {
        Properties properties = new Properties();;
        try(InputStream is = Socks5Server.class.getResourceAsStream("/server.properties")){
            properties.load(is);
        }
        catch (IOException ex){
            logger.error("Error loading properties", ex);
            throw ex;
        }

        String ip = properties.getProperty("ip","0.0.0.0");
        int port = Integer.parseInt(properties.getProperty("port", "1080"));
        int backLog = Integer.parseInt(properties.getProperty("back-log", "5"));
        int maxClients = Integer.parseInt(properties.getProperty("max-clients", "10"));
        AuthenticationMethod authenticationMethod = AuthenticationMethod.fromName(properties.getProperty("auth-method", "none"));
        ServerConfig config = this;
        config.setIp(ip);
        config.setPort(port);
        config.setBackLog(backLog);
        config.setMaxClients(maxClients);
        config.setAuthenticationMethod(authenticationMethod);
    }


}
