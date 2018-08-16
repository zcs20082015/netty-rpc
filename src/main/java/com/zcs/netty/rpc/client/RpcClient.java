package com.zcs.netty.rpc.client;

import com.zcs.netty.rpc.anno.RpcReference;
import com.zcs.netty.rpc.registry.RegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RpcClient implements ApplicationListener<ContextRefreshedEvent> {

    private ApplicationContext applicationContext;

    @Value("${spring.registry.address}")
    private String registryAddress;

    //@Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        System.out.println("初始化rpcClient");
        init();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent ctx) {
        applicationContext=ctx.getApplicationContext();
        System.out.println("初始化rpcClient");
        init();
    }



    public void init(){

        try {

            //链接注册中心
            RegistryCenter.connectZookeeper(registryAddress);

            //获取所有引用接口
            List<Class> refs=getAllReference();

            //从注册中心获取服务列表
            for(Class cla:refs){
                RegistryCenter.discovery(cla.getName());
            }


        } catch (Exception e) {
        	e.printStackTrace();

        }

    }

    /**
     *@Desc: 获取所有引用接口
     *@Author: zhengcs
     *@Date: 2018/8/16 14:14
     *@Modified:
     */
    private List<Class> getAllReference(){
        List<Class> list=new ArrayList<>();
        Map<String, Object> controllerBeans=applicationContext.getBeansWithAnnotation(Controller.class);
        for(Object bean:controllerBeans.values()){
            Field[] fields=bean.getClass().getDeclaredFields();
            for(Field field:fields){
                RpcReference ref=field.getAnnotation(RpcReference.class);
                if(null!=ref){
                    list.add(field.getType());
                }
            }
        }

        return list;
    }
}
