package com.zcs.netty.rpc.server;

import com.alibaba.fastjson.JSON;
import com.zcs.netty.rpc.domain.RpcRequest;
import com.zcs.netty.rpc.domain.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.util.Map;

public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest>{
    private Map<String,Class<?>> services;

    public ServerHandler(Map services){
        this.services=services;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, RpcRequest request) throws Exception {

        RpcResponse response = new RpcResponse();
        try {
            Object result = handle(request);
            response.setData(result);
        } catch (Throwable t) {
            response.setRetCode("9999");
            response.setRetMsg("交易失败");
        }
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RpcRequest request) throws Throwable {
        String interfaceName = request.getInterfaceName();
        Class serviceClass = services.get(interfaceName);
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParamsTypes();
        Object[] parameters = request.getParams();

        //java反射
        /*Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);*/

        //cglib反射
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceClass.newInstance(), parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("系统异常");
        cause.printStackTrace();
        ctx.close();
    }
}
