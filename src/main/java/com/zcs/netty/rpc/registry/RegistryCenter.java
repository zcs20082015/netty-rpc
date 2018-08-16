package com.zcs.netty.rpc.registry;

import com.zcs.netty.rpc.base.AbstractLogger;
import com.zcs.netty.rpc.client.RpcProxy;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
*@Desc: 注册中心
*@Author: zhengcs
*@Date: 2018/8/15 16:55
*@Modified: 
*/
public class RegistryCenter {
    private static final Logger log= LoggerFactory.getLogger(RegistryCenter.class);

    private static int TIMEOUT=10000;

    private static ZooKeeper zooKeeper;

    private static CountDownLatch lanch=new CountDownLatch(1);


    /**
    *@Desc: 服务注册
    *@Author: zhengcs
    *@Date: 2018/8/15 16:29
    *@Modified: 
    */
    public static void register(String interfaceName,String localAddress) throws Exception{
        log.info("服务"+interfaceName+"开始注册。。。");
        createNode(interfaceName,localAddress);
    }

    /**
    *@Desc: 服务发现
    *@Author: zhengcs
    *@Date: 2018/8/15 19:24
    *@Modified: 
    */
    public static List<String> discovery(String interfaceName) throws Exception{

        List<String> result=new ArrayList<>();
        if(zooKeeper.exists("/myrpc/"+interfaceName,new clientWatcher())==null){
            log.info("服务"+interfaceName+"不存在");
            return result;
        }
        byte[] bIP=zooKeeper.getData("/myrpc/"+interfaceName,true,null);
        String ips=new String(bIP,"UTF-8");
        log.info("服务"+interfaceName+"发现："+ips);


        if(ips.length()>1){
            result=Arrays.asList(ips.split(","));
            RpcProxy.services.put(interfaceName,result);
        }

        return result;
    }


    /**
    *@Desc: 链接注册中心
    *@Author: zhengcs
    *@Date: 2018/8/15 16:28
    *@Modified: 
    */
    public static void connectZookeeper(String registryAddress) throws Exception{
        if(null!=zooKeeper){
            return;
        }
        zooKeeper=new ZooKeeper(registryAddress, TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if(watchedEvent.getState()==Event.KeeperState.SyncConnected){
                    lanch.countDown();
                }
            }
        });

        lanch.await();
    }


    /**
    *@Desc: 创建节点
    *@Author: zhengcs
    *@Date: 2018/8/15 17:08
    *@Modified: 
    */
    private static void createNode(String interfaceName,String localAddress) throws Exception{
        if (zooKeeper.exists("/myrpc", false) == null) {
            zooKeeper.create("/myrpc", "true".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        if (zooKeeper.exists("/myrpc/"+interfaceName, false) == null) {
            zooKeeper.create("/myrpc/"+interfaceName, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        }

        //获取数据
        byte[] bIP=zooKeeper.getData("/myrpc/"+interfaceName,false,null);
        String ips=new String(bIP,"UTF-8");
        if(StringUtils.isEmpty(ips)){
            ips=localAddress;
        }else{
            if(ips.indexOf(localAddress)<0){
                ips+=","+localAddress;
            }

        }

        zooKeeper.setData("/myrpc/"+interfaceName,ips.getBytes("UTF-8"),-1);
        log.info("服务"+interfaceName+"注册完成："+ips);
    }

    static class clientWatcher implements Watcher{
        @Override
        public void process(WatchedEvent event) {
            log.info("监控事件："+event.toString());
            String interfaceName=event.getPath().substring(event.getPath().indexOf("/myrpc/")+7);
            try {
                RegistryCenter.discovery(interfaceName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
