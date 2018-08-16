package com.zcs.netty.rpc.controller;

import com.zcs.netty.rpc.anno.RpcReference;
import com.zcs.netty.rpc.base.AbstractLogger;
import com.zcs.netty.rpc.client.RpcProxy;
import com.zcs.netty.rpc.domain.UserInfo;
import com.zcs.netty.rpc.service.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
@RequestMapping("user")
public class UserController extends AbstractLogger{


    @RpcReference
    private IUserService userService;

    @RequestMapping("get")
    @ResponseBody
    public UserInfo getUser(){
        userService= RpcProxy.getProxy(IUserService.class);
        UserInfo user=userService.getUser(getCorrelationID(),"1");
        info("查询结果：",user);

        return user;
    }


    @Override
    protected String getCorrelationID() {
        return UUID.randomUUID().toString();
    }
}
