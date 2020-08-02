package com.cyz.socks5.server;


import com.cyz.socks5.server.enums.AddrTypeEnum;
import org.junit.Assert;
import org.junit.Test;

public class HostResolverTest {

    @Test
    public void resolveIpv4Test() {
        byte[] bts = new byte[]{127, 0, 0, 1};
        String resolved = new HostResolver().resolveHost(AddrTypeEnum.IPV4.getCode(), bts);
        Assert.assertEquals("127.0.0.1", resolved);

        bts = new byte[]{-1, -1, -1, -1};
        resolved = new HostResolver().resolveHost(AddrTypeEnum.IPV4.getCode(), bts);
        Assert.assertEquals("255.255.255.255", resolved);
    }

    @Test
    public void resolveIpv6Test() {
        byte[] bts = new byte[]{127, 0, 0, 1, -1, -1, -1, -1, 127, 0, 0, 1, -1, -1, -1, -1,};
        String resolved = new HostResolver().resolveHost(AddrTypeEnum.IPV6.getCode(), bts);
        Assert.assertEquals("7F00:0001:FFFF:FFFF:7F00:0001:FFFF:FFFF", resolved);
    }
}