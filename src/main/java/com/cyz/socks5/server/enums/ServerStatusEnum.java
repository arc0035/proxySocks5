package com.cyz.socks5.server.enums;

/**
 * 服务器状态
 */
public enum ServerStatusEnum {

    /**
     * 认证还未开始，协商认证方案
     */
    Init,

    /**
     * 认证中
     */
    Authenticating,

    /**
     * 认证阶段完成
     */
    CommandProcess,

    /**
     * 已经连接完毕
     */
    Connected,

    /**
     * 已经监听某个地址
     */
    Binded,


    Relaying,

    Udp,

    /**
     * 客户端断开连接
     */
    Disconnected;

}
