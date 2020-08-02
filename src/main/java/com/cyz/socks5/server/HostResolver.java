package com.cyz.socks5.server;

import com.cyz.socks5.server.enums.AddrTypeEnum;
import com.cyz.socks5.server.enums.ClientErrorEnum;
import com.cyz.socks5.server.error.ProxyServerException;
import com.cyz.socks5.server.host.DomainHostResolver;
import com.cyz.socks5.server.host.HostAddrResolver;
import com.cyz.socks5.server.host.Ipv4HostResolver;
import com.cyz.socks5.server.host.Ipv6HostResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HostResolver {

    private static final Logger logger = LoggerFactory.getLogger(HostResolver.class);

    private Map<Integer, HostAddrResolver> hostResolvers = new HashMap<>();

    public HostResolver(){
        init();
    }

    public void init(){
        hostResolvers.put(AddrTypeEnum.IPV4.getCode(), new Ipv4HostResolver());
        hostResolvers.put(AddrTypeEnum.DOMAIN.getCode(), new DomainHostResolver());
        hostResolvers.put(AddrTypeEnum.IPV6.getCode(), new Ipv6HostResolver());
    }

    public byte[] hostToBytes(int addrType, String host){
        HostAddrResolver hostResolver = this.hostResolvers.get(addrType);
        return hostResolver.toBytes(host);
    }

    //不做扩展性考虑，所以用if else
    public String resolveHost(int addrType, byte[] dstAddr) {
        try {
            HostAddrResolver hostResolver = this.hostResolvers.get(addrType);
            if(hostResolver == null){
                throw new ProxyServerException(ClientErrorEnum.InvalidAddressType);
            }
            return hostResolver.resolve(dstAddr);

        } catch (ProxyServerException pse) {
            throw pse;
        }
        catch (Exception ex){
            logger.warn("{}", ex.getMessage());
            return null;
        }
    }

}
