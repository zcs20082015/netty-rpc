package com.zcs.netty.rpc.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class UtilTool {

    public static InetAddress getLocalHostLANAddress(){
        try {
            Enumeration<NetworkInterface> interfaces=null;
            interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresss = ni.getInetAddresses();
                while(addresss.hasMoreElements())
                {
                    InetAddress nextElement = addresss.nextElement();
                    String hostAddress = nextElement.getHostAddress();
                    if(nextElement instanceof Inet4Address){
                        System.out.println("本机IP地址为：" +hostAddress);
                        if(!hostAddress.split("\\.")[3].equals("1")){
                            return nextElement;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {

        System.out.println(getLocalHostLANAddress().getHostAddress());
    }
}
