package com.cyz.socks5.server;

import com.cyz.socks5.server.enums.CommonErrorEnum;
import com.cyz.socks5.server.error.ProxyServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class UdpRelayingTask {

    private static final Logger logger = LoggerFactory.getLogger(UdpRelayingTask.class);
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

    public static void register(DatagramChannel client, DatagramChannel target){
        try{
            lock.lock();
            registering = true;
            selector.wakeup();
            client.configureBlocking(false);
            target.configureBlocking(false);
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
        new Thread(() -> mainLoop(), "udp-relaying-thread").start();
    }

    private static void mainLoop(){
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
            catch (Exception ex){
                logger.error("Should not reach here", ex);
            }
        }
    }

    private static void handleRead(SelectionKey key){
        try{
            DatagramChannel srcChannel = (DatagramChannel)key.channel();
            ByteBuffer readBuf = channelBuf.get(srcChannel);
            DatagramChannel tgtChannel = channelMap.get(srcChannel);
            srcChannel.receive(readBuf);
            int nread = readBuf.position();
            System.out.println("读到数据:"+nread);
            if(nread == 0){
                return;
            }
            readBuf.flip();
            tgtChannel.write(readBuf);
            readBuf.clear();
        }
        catch (IOException ex){
            logger.error("", ex);
        }
    }

}







