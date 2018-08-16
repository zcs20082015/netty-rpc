package com.zcs.netty.rpc.domain;

public class RpcRequest {

    private String interfaceName;

    private String methodName;

    private Class<?>[] paramsTypes;

    private Object[] params;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParamsTypes() {
        return paramsTypes;
    }

    public void setParamsTypes(Class<?>[] paramsTypes) {
        this.paramsTypes = paramsTypes;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }
}
