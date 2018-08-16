package com.zcs.netty.rpc.base;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLogger {

    private static final Logger log=LoggerFactory.getLogger(AbstractLogger.class);

    protected void info(String msg){
        log.info("[{}]{}",getCorrelationID(),msg);
    }

    protected void info(String msg,Object obj){
        log.info("[{}]{}:{}",getCorrelationID(),msg, JSON.toJSONString(obj) );

    }

    protected void error(String msg,Exception e){
        log.error("[{}]{}",getCorrelationID(),msg,e);
    }

    protected abstract String getCorrelationID();
}
