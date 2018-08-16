package com.zcs.netty.rpc.service;

import com.zcs.netty.rpc.domain.UserInfo;

/**
 * @Author: zhengcs
 * @Desc:
 * @Date: 2018/8/14 21:00
 **/
public interface IUserService {

    /**
    *@Desc:
    *@Author: zhengcs
    *@Date: 2018/8/14 21:16
    *@Modified: 
    */
    public UserInfo getUser(String correlationID,String userId);
}
