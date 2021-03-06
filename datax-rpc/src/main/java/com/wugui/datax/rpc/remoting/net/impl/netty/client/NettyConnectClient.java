package com.wugui.datax.rpc.remoting.net.impl.netty.client;

import com.wugui.datax.rpc.remoting.invoker.XxlRpcInvokerFactory;
import com.wugui.datax.rpc.remoting.net.common.ConnectClient;
import com.wugui.datax.rpc.remoting.net.impl.netty.codec.NettyDecoder;
import com.wugui.datax.rpc.remoting.net.impl.netty.codec.NettyEncoder;
import com.wugui.datax.rpc.remoting.net.params.Beat;
import com.wugui.datax.rpc.remoting.net.params.XxlRpcRequest;
import com.wugui.datax.rpc.remoting.net.params.XxlRpcResponse;
import com.wugui.datax.rpc.serialize.Serializer;
import com.wugui.datax.rpc.util.IpUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * netty pooled client
 *
 * @author xuxueli
 */
public class NettyConnectClient extends ConnectClient {

    //Netty内部都是通过线程在处理各种数据，EventLoopGroup就是用来管理调度他们的，注册Channel，管理他们的生命周期
    private EventLoopGroup group;
    //netty channel
    private Channel channel;


    @Override
    public void init(String address, final Serializer serializer, final XxlRpcInvokerFactory xxlRpcInvokerFactory) throws Exception {
        final NettyConnectClient thisClient = this;

        Object[] array = IpUtil.parseIpPort(address);
        String host = (String) array[0]; //ip
        int port = (int) array[1]; //port


        this.group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap(); //netty bootstrap
        bootstrap.group(group)
                .channel(NioSocketChannel.class) //Channel包含了ChannelPipeline，ChannelPipeline内部包含了N个handler，每一个handler都是由一个线程去执行；
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new IdleStateHandler(0,0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS))    // beat N, close if fail
                                .addLast(new NettyEncoder(XxlRpcRequest.class, serializer))
                                .addLast(new NettyDecoder(XxlRpcResponse.class, serializer))
                                .addLast(new NettyClientHandler(xxlRpcInvokerFactory, thisClient));
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        this.channel = bootstrap.connect(host, port).sync().channel(); //真正连接netty server

        // valid
        if (!isValidate()) {
            close();
            return;
        }

        logger.debug(">>>>>>>>>>> xxl-rpc netty client proxy, connect to server success at host:{}, port:{}", host, port);
    }


    @Override
    public boolean isValidate() {
        if (this.channel != null) {
            return this.channel.isActive();
        }
        return false;
    }

    @Override
    public void close() {
        if (this.channel != null && this.channel.isActive()) {
            this.channel.close();        // if this.channel.isOpen()
        }
        if (this.group != null && !this.group.isShutdown()) {
            this.group.shutdownGracefully();
        }
        logger.debug(">>>>>>>>>>> xxl-rpc netty client close.");
    }

    //真正发送到netty server
    @Override
    public void send(XxlRpcRequest xxlRpcRequest) throws Exception {
        this.channel.writeAndFlush(xxlRpcRequest).sync();
    }
}
