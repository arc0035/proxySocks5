package com.cyz.socks5.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 专门做流量转发的线程.
 */
public class TcpRelayingTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TcpRelayingTask.class);
    private boolean started;
    private ConcurrentHashMap<SocketChannel, SocketChannel>  socksMap;
    private ConcurrentHashMap<SocketChannel, ByteBuffer>  bufMap;
    private ConcurrentHashMap<SocketChannel, Boolean> clients;
    private ConcurrentHashMap<SocketChannel, SocketAddress> remoteAddrs;
    private Selector selector;
    private ReentrantLock selectorLock;

    private static TcpRelayingTask INSTANCE;

    static {
        try{
            INSTANCE = new TcpRelayingTask();
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private TcpRelayingTask() throws IOException{
        this.started = false;
        this.socksMap = new ConcurrentHashMap<>();
        this.bufMap = new ConcurrentHashMap<>();
        this.clients = new ConcurrentHashMap<>();
        this.remoteAddrs = new ConcurrentHashMap<>();
        this.selector = Selector.open();
        this.selectorLock = new ReentrantLock();
    }

    public static void start() {
        INSTANCE.startTask();
    }

    public static boolean register(SocketChannel channel1, SocketChannel channel2){
        return INSTANCE.registerImpl(channel1, channel2);
    }


    private boolean registerImpl(SocketChannel channel1, SocketChannel channel2) {
        try{
            //Configure blocking to false after blocking reads will make the connection bad?!?!?!
            channel1.configureBlocking(false);
            channel2.configureBlocking(false);
            try{
                this.selectorLock.lock();
                this.selector.wakeup();//唤醒
                channel1.register(selector, SelectionKey.OP_READ);
                channel2.register(selector, SelectionKey.OP_READ);
            }
            finally {
                this.selectorLock.unlock();
            }
            socksMap.put(channel1, channel2);
            socksMap.put(channel2, channel1);
            bufMap.putIfAbsent(channel1, ByteBuffer.allocate(4096));
            bufMap.putIfAbsent(channel2, ByteBuffer.allocate(4096));
            remoteAddrs.putIfAbsent(channel1, channel1.getRemoteAddress());
            remoteAddrs.putIfAbsent(channel2, channel2.getRemoteAddress());
            clients.putIfAbsent(channel1, true);
            return true;
        }
        catch (IOException ex){
            ex.printStackTrace();
            logger.error("Error happening on registering channels", ex);
            return false;
        }
    }

    private synchronized void startTask() {
        if(started){
            logger.warn("alread started");
            return;
        }
        new Thread(this,"tcp-relaying-thread").start();
        started = true;
    }



    @Override
    public void run() {
        int nselect;
        while (true){
            try{
                nselect = this.selector.select();//This can block register。
                if(nselect <= 0){
                    //可以被唤醒
                    try{
                        this.selectorLock.lock();//用锁来阻止再度堕入select睡眠
                    }
                    finally {
                        this.selectorLock.unlock();
                    }
                    continue;
                }
                logger.info("channels selected:{}",nselect);
                Set<SelectionKey> keys = this.selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()){
                    SelectionKey selectionKey = it.next();
                    it.remove();
                    handleReadyChannel(selectionKey);
                }
            }
            catch (Exception ex){
                logger.error("Unexpected ",ex);
            }

        }
    }

    private void handleReadyChannel(SelectionKey ready) throws IOException{
        if(ready.isReadable()){
            handleReadable(ready);
        }
        else{
            logger.error("Not readable");
        }
    }

    private boolean handlePeerFailed(SocketChannel remote) {
        if(this.clients.get(remote) != null){
            logger.info("Client disconnected, try reconnect");
            SocketChannel client = remote;
            SocketChannel target = this.socksMap.get(client);
            try{
                //clearOld
                SocketAddress addr = cleanSocketResource(client);
                client.close();
                SocketChannel newClient = SocketChannel.open(addr);
                if(newClient.isConnected()){
                    registerImpl(newClient, target);
                    logger.info("Reconnect successful {}", newClient.getRemoteAddress());
                    return true;
                }
            }
            catch (IOException ex){
                logger.error("Reconnect failed, releasing peer socket resource.", ex);
                cleanSocketResource(target);
                return false;
            }
        }
        else{
            logger.info("Target host disconnected, try reconnect");
            SocketChannel target = remote;
            SocketChannel client = this.socksMap.get(target);
            try{
                //clearOld
                SocketAddress addr = cleanSocketResource(target);
                SocketChannel newTarget = SocketChannel.open(addr);
                if(newTarget.isConnected()){
                    registerImpl(client, newTarget);
                    logger.info("Reconnect successful {}", newTarget.getRemoteAddress());
                    return true;
                }
            }
            catch (IOException ex){
                logger.error("Reconnect failed, releasing peer socket resource.", ex);
                cleanSocketResource(client);
                return false;
            }
        }
        return false;
    }

    private void handleReadable(SelectionKey readableKey) throws IOException{
        SocketChannel sc = (SocketChannel) readableKey.channel();
        ByteBuffer bb = doRead(sc);
        if(bb != null){
            SocketChannel partner = this.socksMap.get(sc);
            doWrite(partner, bb);
        }
    }

    private ByteBuffer doRead(SocketChannel sc){
        ByteBuffer bb = this.bufMap.get(sc);
        try {
            int n = sc.read(bb);
            if (n == -1) {
                //https://stackoverflow.com/questions/7937908/java-selector-returns-selectionkey-with-op-read-without-data-in-infinity-loop-af
                //or check OP_READ, select can return on remote closed
                throw new IOException("Read returns -1");
            }
            logger.info("relaying read {} bytes", n);
            return bb;
        }
        catch (IOException ex){
            logger.error("Reading with exception on peer {}:", remoteAddrs.get(sc), ex);
            handlePeerFailed(sc);
            return null;
        }
    }

    private void doWrite(SocketChannel partner, ByteBuffer bb){
        try{
            bb.flip();
            int n = partner.write(bb);
            logger.info("relaying write {} bytes", n);
            bb.compact();
        }
        catch (IOException ex){
            logger.error("Reading with exception on peer, {}", remoteAddrs.get(partner), ex);
            handlePeerFailed(partner);
        }
    }

    private SocketAddress cleanSocketResource(SocketChannel channel){
        this.socksMap.remove(channel);
        this.bufMap.remove(channel);
        this.clients.remove(channel);
        SocketAddress addr = this.remoteAddrs.remove(channel);
        try{
            channel.close();
        }
        catch (Exception ex){}
        return addr;
    }

}
