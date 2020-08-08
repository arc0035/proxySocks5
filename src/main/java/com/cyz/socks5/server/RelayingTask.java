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
public class RelayingTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RelayingTask.class);
    private boolean started;
    private ConcurrentHashMap<SocketChannel, SocketChannel>  socksMap;
    private ConcurrentHashMap<SocketChannel, ByteBuffer>  bufMap;
    private ConcurrentHashMap<SocketChannel, Boolean> clients;
    private ConcurrentHashMap<SocketChannel, SocketAddress> remoteAddrs;
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
                ex.printStackTrace();
                logger.error("error",ex);
            }

        }
    }

    private void handleReadyChannel(SelectionKey ready) throws IOException{
        handleReadable(ready);

    }

    private HandleChannelResult handleReadable(SelectionKey readableKey) throws IOException{
        SocketChannel sc = (SocketChannel) readableKey.channel();
        try {
            if (!readableKey.isReadable()) {
                return HandleChannelResult.NotType;
            }
            //Read !
            ByteBuffer bb = this.bufMap.get(sc);
            int n = sc.read(bb);
            if (n == -1) {
                //https://stackoverflow.com/questions/7937908/java-selector-returns-selectionkey-with-op-read-without-data-in-infinity-loop-af
                //or check OP_READ, select can return on remote closed
                logger.error("读到-1，远程关闭了啊？{}", remoteAddrs.get(sc));
                return HandleChannelResult.Failed;
            }
            System.out.println("读取完毕，字节数"+n);
            //Write !
            SocketChannel partner = this.socksMap.get(sc);
            bb.flip();
            n = partner.write(bb);
            bb.clear();
            System.out.println("写入完毕，字节数"+n);
            return HandleChannelResult.Success;

        }
        catch (CancelledKeyException|ClosedChannelException ex){
            //CancelledKeyException is throwned by isReadable(). This often caused by channel is closed and automatically deregistered
            //ClosedChannelException is throwned by common io operations like getRemoteAddress
            //ex.printStackTrace();
            System.out.println("远程关闭了。Remote addr:"+remoteAddrs.get(sc));
            return HandleChannelResult.Failed;
        }
        catch (IOException ex){
            System.out.println("神奇的错误啊");
            ex.printStackTrace();
            return HandleChannelResult.Failed;
        }
    }

    /*
    private HandleChannelResult handleWritable(SelectionKey writableKey) throws IOException{
        SocketChannel sc = (SocketChannel)writableKey.channel();
        try{
            if(!writableKey.isWritable()){
                return HandleChannelResult.NotType;
            }
            logger.info("writable");
            ByteBuffer bb = this.bufMap.get(socksMap.get(sc));
            bb.flip();
            int n = sc.write(bb);
            bb.clear();
            if(n == -1){
                return HandleChannelResult.Failed;
            }
            sc.register(selector, SelectionKey.OP_READ);//To prevent always writable.
            return HandleChannelResult.Success;
        }
        catch (CancelledKeyException|ClosedChannelException ex){
            ex.printStackTrace();
            System.out.println("Remote addr:"+remoteAddrs.get(sc));
            return HandleChannelResult.Failed;
        }
    }

     */

    private enum HandleChannelResult{

        Failed,
        Success,
        NotType

    }

    /*
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
                ex.printStackTrace();
                onPeerDisconnect(readKey, writeKey);
            }
        }
    }

    private void onPeerDisconnect(SelectionKey readKey, SelectionKey writeKey) throws IOException{
        logger.info("Peer close the socket. Renew for target or close relay for client");
        SelectionKey clientKey = isClient(readKey)?readKey:writeKey;
        SelectionKey tgtKey = isClient(readKey)?writeKey:readKey;
        if(!clientKey.isValid()){
            closeEverything(clientKey, tgtKey);
            return;
        }
        fixConnection(tgtKey);
        logger.info("target disconnected. Reconnected");
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

    private void fixConnection(SelectionKey tgtKey) throws IOException{
        SocketChannel tgtChannel = (SocketChannel) tgtKey.channel();
        SocketChannel clientChannel = this.socksMap.get(tgtChannel);
        SocketAddress remoteAddress = tgtChannel.getRemoteAddress();//TODO:这里可能报错，需要提前把remoteAddress读出来注册
        this.socksMap.remove(tgtChannel);
        this.bufMap.remove(tgtChannel);
        SocketChannel newChannel = SocketChannel.open();
        newChannel.connect(remoteAddress);
        registerImpl(newChannel, clientChannel);
    }


     */

}
