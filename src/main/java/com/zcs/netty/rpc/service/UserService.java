package com.zcs.netty.rpc.service;

import com.zcs.netty.rpc.anno.RpcService;
import com.zcs.netty.rpc.domain.UserInfo;
import org.springframework.stereotype.Service;

@RpcService(IUserService.class)
@Service
public class UserService implements IUserService {

    @Override
    public UserInfo getUser(String correlationID, String userId) {

        UserInfo user=new UserInfo();
        user.setUserId("1");
        user.setUserName("韩逗比");

        return user;

    }
}
