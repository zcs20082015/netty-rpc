package com.zcs.netty.rpc.server;

import com.zcs.netty.rpc.anno.RpcService;
import com.zcs.netty.rpc.domain.RpcRequest;
import com.zcs.netty.rpc.domain.RpcResponse;
import com.zcs.netty.rpc.registry.RegistryCenter;
import com.zcs.netty.rpc.util.RpcDecoder;
import com.zcs.netty.rpc.util.RpcEncoder;
import com.zcs.netty.rpc.util.UtilTool;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Service
public class RpcServer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Value("${spring.registry.address}")
    private String registryAddress;

    @Value("${spring.provider.port}")
    private String port;

    private String localIP;

    private Map<String,Class<?>> services;

    /**
     * @Desc: 服务端初始化
     * @Author: zhengcs
     * @Date: 2018/8/15 20:49
     * @Modified:
     */
    public void init() {

        try {
            //获取本机ip
            localIP= UtilTool.getLocalHostLANAddress().getHostAddress();
            //本机连接地址
            String localAddress=localIP+":"+port;

            //链接注册中心
            RegistryCenter.connectZookeeper(registryAddress);

            //启动本机服务
            start();

            //查询所有rpcService并注册
            services=new HashMap<>();
            Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class); // 获取所有带有 RpcService 注解的 Spring Bean
            if (null!=serviceBeanMap && serviceBeanMap.size()>0) {
                for (Object serviceBean : serviceBeanMap.values()) {
                    String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                    services.put(interfaceName,serviceBean.getClass());
                    //服务注册
                    RegistryCenter.register(interfaceName,localAddress);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
    *@Desc: 单线程启动服务端
    *@Author: zhengcs
    *@Date: 2018/8/16 21:19
    *@Modified: 
    */
    private void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup bossGroup=new NioEventLoopGroup();
                EventLoopGroup workerGroup=new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap=new ServerBootstrap();
                    bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {

                                @Override
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    socketChannel.pipeline()
                                            .addLast(new RpcDecoder(RpcRequest.class))
                                            .addLast(new RpcEncoder(RpcResponse.class))
                                            .addLast(new ServerHandler(services));
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG,128)
                            .childOption(ChannelOption.SO_KEEPALIVE,true);

                    ChannelFuture future=bootstrap.bind(localIP,Integer.parseInt(port)).sync();
                    future.channel().closeFuture().sync();

                } catch (Exception e) {
                    e.printStackTrace();

                }finally {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        }).start();
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        applicationContext = ctx;
        System.out.println("rpcServer开始初始化");
        init();
    }
}
