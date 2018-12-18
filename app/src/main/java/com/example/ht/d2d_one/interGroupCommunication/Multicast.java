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

        import android.os.Message;
        import android.util.Log;

        import java.io.IOException;
        import java.net.DatagramPacket;
        import java.net.Inet4Address;
        import java.net.InetAddress;
        import java.net.InetSocketAddress;
        import java.net.MulticastSocket;
        import java.net.NetworkInterface;
        import java.net.UnknownHostException;
        import java.util.Enumeration;

        import static com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior.messageHandler;
        import static com.example.ht.d2d_one.intraGroupCommunication.IntraCommunication.main2ActivityMessagHandler;

public class Multicast extends Thread{
    private  String label;
    private int port;
    private String content = "1";
    private String multiAddress;
    private String ipAddr;
    public Multicast(){}
    public Multicast(int port,String label,String multiAddress,String content){
        this.multiAddress = multiAddress;
        this.port = port;
        this.label = label;
        this.content = content;
    }
    public Multicast(int port,String label,String multiAddress,String wifiIP,String content){
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
                Log.d("network interface", NetworkInterface.getNetworkInterfaces().nextElement().getName());
                Log.d("network interface",multicastSocket.getInterface().toString());
                NetworkInterface intf = null;
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                    intf = en.nextElement();
                    String iface = intf.getName();
                    if(iface.matches(".*" +"p2p0"+ ".*")){
                        break;
                    }
                }
                multicastSocket.setNetworkInterface(intf);
                //这样写我认为和下面是一样的，但是这样写不行，出现socketException异常
                //multicastSocket.setInterface(InetAddress.getByName("192.168.49.1"));
                // multicastSocket.joinGroup(multiGroup);
                /**
                 * 所以，我的问题是，multicastSocket.setInterface和multicastSocket.setNetworkInterface的作用以及区别。
                 * 为什么写成joinGroup(new InetSocketAddress(multiGroup,port),intf)就有效，而joinGroup(multiGroup)无效，在这里，我也是设置了网卡的啊？？
                 * 前面的joinGroup也是绑定一个网卡啊????
                 */
                multicastSocket.joinGroup(new InetSocketAddress(multiGroup,port),intf);
                DatagramPacket sendPacket = new DatagramPacket(content.getBytes(),content.length(),multiGroup,port);
                multicastSocket.setTimeToLive(24);
                multicastSocket.send(sendPacket);
                Log.d("广播内容",content);
                multicastSocket.close();
            }
            //组内的组播接收，使用p2p网卡，在离开的时候关闭此multicastaSocket
            else if(label.equals("recv")){
                while(true){
                    MulticastSocket multicastSocket = new MulticastSocket(port);
                    byte [] buf = new byte[3000];
                    DatagramPacket recv = new DatagramPacket(buf,buf.length);
                    InetAddress multiGroup = createMulticastGroup(multiAddress);
                    Log.d("组员设备的IP地址",ipAddr);
                    //multicastSocket.setInterface(InetAddress.getByName(ipAddr));
                    //multicastSocket.joinGroup(multiGroup);
                    NetworkInterface itf = NetworkInterface.getByInetAddress(InetAddress.getByName(ipAddr));
                    multicastSocket.joinGroup(new InetSocketAddress(multiGroup,port),itf);
                    multicastSocket.receive(recv);
                    String receiver = new String(recv.getData()).trim();
                    Log.d("来自组主的组内组播消息：：：：",receiver);
                    multicastSocket.close();
                    //将收到的组播信息发送给LC连接的GO
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public InetAddress createMulticastGroup(String multiCastAddress){
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

