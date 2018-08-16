package com.zcs.netty.rpc.client;

import com.alibaba.fastjson.JSON;
import com.zcs.netty.rpc.domain.RpcRequest;
import com.zcs.netty.rpc.domain.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.Line;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RpcProxy {

    private static Logger log= LoggerFactory.getLogger(RpcProxy.class);

    public static Map<String,List<String>> services=new HashMap<>();


    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcRequest request = new RpcRequest(); // 创建并初始化 RPC 请求
                        request.setInterfaceName(interfaceClass.getName());
                        request.setMethodName(method.getName());
                        request.setParamsTypes(method.getParameterTypes());
                        request.setParams(args);

                        List<String> address=services.get(interfaceClass.getName());
                        if(null==address||address.size()==0){
                            throw new RuntimeException("不存在远程接口："+interfaceClass.getName());
                        }
                        //随机取一个提供方
                        int num= new Random().nextInt(address.size());
                        String[] ipPort=address.get(num).split(":");
                        ClientHandler handler = new ClientHandler(ipPort[0], Integer.parseInt(ipPort[1]));
                        RpcResponse response = null;
                        try {
                            response=handler.send(request); // 通过 RPC 客户端发送 RPC 请求并获取 RPC 响应
                        } catch (Exception e) {
                        	//异常尝试重新选取服务节点进行二次请求
                            if(address.size()>1){
                                int num2=0;
                                while (num2==num){
                                    num2= new Random().nextInt(address.size());
                                }
                                ipPort=address.get(num).split(":");
                                handler = new ClientHandler(ipPort[0], Integer.parseInt(ipPort[1])); // 初始化 RPC 客户端
                                response=handler.send(request);
                            }else{
                                e.printStackTrace();
                                throw new RuntimeException("请求异常");
                            }

                        }

                        if(!"0000".equals(response.getRetCode())){
                            throw new RuntimeException(response.getRetMsg());
                        }

                        return response.getData();
                    }
                }
        );
    }
}
