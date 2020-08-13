package com.cyz.socks5.server;

import com.cyz.socks5.server.enums.CommonErrorEnum;
import com.cyz.socks5.server.error.ProxyServerException;

import java.io.IOException;
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
    private static Map<DatagramChannel, DatagramChannel> channels;
    private static AtomicBoolean started;

    private static ReentrantLock lock;
    private static Condition condition;
    private static boolean registering;
    static {
        try{
            selector = Selector.open();
            channels = new HashMap<>();
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
            channels.put(client, target);
            channels.put(target, client);
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
                        //handleRead();
                    }

                    it.remove();
                }
            }
            catch (IOException ex){}
        }
    }

}







