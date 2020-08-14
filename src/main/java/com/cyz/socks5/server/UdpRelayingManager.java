package com.cyz.socks5.server;

import com.cyz.socks5.server.enums.CommonErrorEnum;
import com.cyz.socks5.server.error.ProxyServerException;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author aaronchu
 * @Description
 * @data 2020/08/13
 */
public class UdpRelayingManager {

    private static Selector selector;
    private static Map<DatagramChannel, DatagramChannel> channelMap;
    private static Map<DatagramChannel, ByteBuffer> channelBuf;
    private static AtomicBoolean started;

    private static ReentrantLock lock;
    private static Condition condition;
    private static boolean registering;
    static {
        try{
            selector = Selector.open();
            channelMap = new HashMap<>();
            channelBuf = new HashMap<>();
            started = new AtomicBoolean(false);
            lock = new ReentrantLock();
            condition = lock.newCondition();
        }
        catch (Exception ex){throw new RuntimeException();}
    }

    //TODO:有没有释放资源的途径?
    public static void register(DatagramChannel client, DatagramChannel target){
        try{
            lock.lock();
            registering = true;
            selector.wakeup();
            client.register(selector, SelectionKey.OP_READ);
            target.register(selector, SelectionKey.OP_READ);
            channelMap.putIfAbsent(client, target);
            channelMap.putIfAbsent(target, client);
            channelBuf.putIfAbsent(client, ByteBuffer.allocate(4096));
            channelBuf.putIfAbsent(target, ByteBuffer.allocate(4096));
            registering = false;
            condition.signal();
        }
        catch (IOException ex){
            throw new ProxyServerException(CommonErrorEnum.SelectorFailed);
        }
        finally {
            lock.unlock();
        }
    }

    public static void start(){
        if(!started.compareAndSet(false, true)){
           return;
        }
        while (true){
            try{
                int nselect = selector.select();
                if(nselect == 0) {//wake up
                    try {
                        lock.lock();
                        while (registering) {
                            condition.await();
                        }
                    } catch (Exception ex) {
                    } finally {
                        lock.unlock();
                    }
                    continue;
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()){
                    SelectionKey key = it.next();
                    if(key.isReadable()){
                        handleRead(key);
                    }

                    it.remove();
                }
            }
            catch (IOException ex){}
        }
    }

    private static void handleRead(SelectionKey key){
        /*
        DatagramChannel srcChannel = (DatagramChannel)key.channel();
        ByteBuffer readBuf = channelBuf.get(srcChannel);
        DatagramChannel tgtChannel = channelMap.get(srcChannel);
        srcChannel.receive(readBuf);
        */
    }

}







