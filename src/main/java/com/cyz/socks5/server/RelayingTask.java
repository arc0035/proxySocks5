package com.cyz.socks5.server;

import com.cyz.socks5.server.error.RelayBrokenException;
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
public class RelayingTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RelayingTask.class);
    private boolean started;
    private ConcurrentHashMap<SocketChannel, SocketChannel>  socksMap;
    private ConcurrentHashMap<SocketChannel, ByteBuffer>  bufMap;
    private ConcurrentHashMap<SocketChannel, Boolean> clients;
    private Selector selector;
    private ReentrantLock selectorLock;

    private static RelayingTask INSTANCE;

    static {
        try{
            INSTANCE = new RelayingTask();
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private RelayingTask() throws IOException{
        this.started = false;
        this.socksMap = new ConcurrentHashMap<>();
        this.bufMap = new ConcurrentHashMap<>();
        this.clients = new ConcurrentHashMap<>();
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
            channel1.configureBlocking(false);
            channel2.configureBlocking(false);
            try{
                this.selectorLock.lock();
                this.selector.wakeup();//唤醒
                channel1.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
                channel2.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
            }
            finally {
                this.selectorLock.unlock();
            }
            socksMap.put(channel1, channel2);
            socksMap.put(channel2, channel1);
            bufMap.putIfAbsent(channel1, ByteBuffer.allocate(4096));
            bufMap.putIfAbsent(channel2, ByteBuffer.allocate(4096));
            clients.putIfAbsent(channel1, true);
            return true;
        }
        catch (IOException ex){
            ex.printStackTrace();
            logger.error("Error happening on mapping channels", ex);
            return false;
        }
    }

    private synchronized void startTask() {
        if(started){
            logger.warn("alread started");
            return;
        }
        new Thread(this,"relaying-thread").start();
        started = true;
    }



    @Override
    public void run() {
        int nselect;
        while (true){
            try{
                nselect = this.selector.select();//This can be blocking
                if(nselect <= 0){
                    //可以被唤醒
                    System.out.println("被唤醒");
                    try{
                        this.selectorLock.lock();//用锁来阻止再度堕入select睡眠
                    }
                    finally {
                        this.selectorLock.unlock();
                    }
                    System.out.println("继续select睡");
                    continue;
                }
                Set<SelectionKey> keys = this.selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                HashMap<SocketChannel,SelectionKey> readyChannels = new HashMap<>();
                while (it.hasNext()){
                    SelectionKey selectionKey = it.next();
                    readyChannels.put((SocketChannel) selectionKey.channel(), selectionKey);
                    it.remove();
                }
                handleReadyChannels(readyChannels);
            }
            catch (Exception ex){
                ex.printStackTrace();
                logger.error("error",ex.getMessage());
            }

        }
    }

    private void handleReadyChannels(HashMap<SocketChannel, SelectionKey> readyChannels) throws IOException{
        for(Map.Entry<SocketChannel, SelectionKey> readyChannel: readyChannels.entrySet()) {
            SocketChannel readChannel = readyChannel.getKey();
            SelectionKey readKey = readyChannel.getValue();
            SocketChannel writeChannel = this.socksMap.get(readChannel);
            SelectionKey writeKey = readyChannels.get(writeChannel);
            try {
                if (!readKey.isReadable()) {
                    continue;
                }
                if (writeKey == null || !writeKey.isWritable()) {
                    logger.warn("peer not writable" + writeChannel.getRemoteAddress());
                    continue;
                }
                ByteBuffer readBuffer = this.bufMap.get(readChannel);
                int total = readChannel.read(readBuffer);
                //https://stackoverflow.com/questions/7937908/java-selector-returns-selectionkey-with-op-read-without-data-in-infinity-loop-af
                if (total == -1) {
                    //Peer(client or tgt) disconnect...
                    throw new ClosedChannelException();
                }
                readBuffer.flip();//flip for write
                int n = 0;
                while (n < total) {
                    n += writeChannel.write(readBuffer);
                    System.out.println("写入" + n);
                }
                readBuffer.clear();
                logger.info("finish relay, data count: {}", total);
            } catch (CancelledKeyException | ClosedChannelException ex) {
                onPeerDisconnect(readKey, writeKey);
            }
        }
    }

    private void onPeerDisconnect(SelectionKey readKey, SelectionKey writeKey){
        logger.info("Peer close the socket. Renew for target or close relay for client");
        if(!readKey.isValid() && !isClient(readKey)){
            fixRelay(readKey, writeKey);
        }
        else if(!writeKey.isValid() && !isClient(readKey)){
            fixRelay(readKey, writeKey);
        }
        else{
            closeEverything( readKey, writeKey);
        }
    }

    private boolean isClient(SelectionKey key){
        return this.clients.containsKey(key.channel());
    }

    private void closeEverything(SelectionKey readKey, SelectionKey writeKey){
        SocketChannel readChannel =(SocketChannel) readKey.channel();
        SocketChannel writeChannel = (SocketChannel)writeKey.channel();
        readKey.cancel();
        writeKey.cancel();
        this.socksMap.remove(readChannel);
        this.socksMap.remove(writeChannel);
        this.bufMap.remove(readChannel);
        this.bufMap.remove(writeChannel);
        try{
            readChannel.close();
            writeChannel.close();
        }
        catch (Exception ex){}

        logger.info("Relay channel closed");
    }

    private void fixRelay(SelectionKey key, SelectionKey key2){
        SocketChannel channel = (SocketChannel)key.channel();
        SocketChannel channel2 = (SocketChannel)key2.channel();
        try{
            SocketAddress remoteAddress = channel.getRemoteAddress();
            this.socksMap.remove(channel);
            this.bufMap.remove(channel);
            SocketChannel newChannel = SocketChannel.open();
            newChannel.connect(remoteAddress);
            registerImpl(newChannel, channel2);
            logger.info("target channel rebuilt");
        }
        catch (Exception ex){
            closeEverything(key, key2);
        }

    }


}
