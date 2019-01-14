package com.example.ht.d2d_one.interGroupCommunication;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MultiCast extends Thread{
    private  String label;
    private int port;
    private String content = "1";
    private String multiAddress;
    private String ipAddr;
    public MultiCast(int port, String label, String multiAddress){
        this.multiAddress = multiAddress;
        this.port = port;
        this.label = label;
    }
    public MultiCast(int port, String label, String multiAddress, String wifiIP, String content){
        this.multiAddress = multiAddress;
        this.port = port;
        this.label = label;
        this.ipAddr = wifiIP;
        this.content = content;
    }
    @Override
    public void run() {
        try{
            if(label.equals("send")){
                MulticastSocket multicastSocket = new MulticastSocket(port);
                InetAddress multiGroup = createMulticastGroup(multiAddress);
                NetworkInterface intf = null;
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                    intf = en.nextElement();
                    String iface = intf.getName();
                    if(iface.matches(".*" +"wlan0"+ ".*")){
                        break;
                    }
                }
                multicastSocket.setNetworkInterface(intf);
                multicastSocket.joinGroup(new InetSocketAddress(multiGroup,port),intf);
                DatagramPacket sendPacket = new DatagramPacket(content.getBytes(),content.length(),multiGroup,port);
                multicastSocket.setTimeToLive(24);
                multicastSocket.send(sendPacket);
                Log.d("广播内容",content);
                multicastSocket.close();
            }
            else if(label.equals("recv")){
                while(true){
                    MulticastSocket multicastSocket = new MulticastSocket(port);
                    byte [] buf = new byte[3000];
                    DatagramPacket recv = new DatagramPacket(buf,buf.length);
                    InetAddress multiGroup = createMulticastGroup(multiAddress);
                    NetworkInterface itf = NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.49.1"));
                    multicastSocket.joinGroup(new InetSocketAddress(multiGroup,port),itf);
                    multicastSocket.receive(recv);
                    String receiver = new String(recv.getData()).trim();
                    Log.d("来自网关的组间组播消息：：：：",receiver);
                    multicastSocket.close();
                    //将收到的组播信息发送给LC连接的GO
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private InetAddress createMulticastGroup(String multiCastAddress){
        InetAddress group = null;
        if(multiCastAddress.compareTo("224.0.0.0")>0&&multiCastAddress.compareTo("239.255.255.255")<0){
            try{
                group = InetAddress.getByName(multiCastAddress);
            }catch (UnknownHostException e){
                e.printStackTrace();
            }
        }
        return group;
    }
    public void leaveMulticastGroup(){
    }
}

