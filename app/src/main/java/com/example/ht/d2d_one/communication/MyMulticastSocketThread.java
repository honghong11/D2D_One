package com.example.ht.d2d_one.communication;

import android.os.Message;
import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.icn.ResourceRequestPacket;
import com.example.ht.d2d_one.interGroupCommunication.Multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior.messageHandler;
import static com.example.ht.d2d_one.intraGroupCommunication.IntraCommunication.main2ActivityMessagHandler;

public class MyMulticastSocketThread extends Thread{
    private  String label;
    private int port;
    private String content = "1";
    private String multiAddress;
    private String p2pIpAddr;
    private String wifiIP;
    private String macOfThisDevice;
    private String goMAC;
    private boolean isRecv;
    public MyMulticastSocketThread(){}
    public MyMulticastSocketThread(int port,String label,String multiAddress,String content){
        this.multiAddress = multiAddress;
        this.port = port;
        this.label = label;
        this.content = content;
    }
    public MyMulticastSocketThread(String goMAC,String macOfThisDevice,int port,String label,String multiAddress,String ipAddr,String wifiIP, boolean isRecv){
        this.goMAC = goMAC;
        this.macOfThisDevice = macOfThisDevice;
        this.multiAddress = multiAddress;
        this.port = port;
        this.label = label;
        this.p2pIpAddr = ipAddr;
        this.wifiIP = wifiIP;
        this.isRecv = isRecv;
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
                    Log.d("组员设备的IP地址",p2pIpAddr);
                    //multicastSocket.setInterface(InetAddress.getByName(ipAddr));
                    //multicastSocket.joinGroup(multiGroup);
                    NetworkInterface itf = NetworkInterface.getByInetAddress(InetAddress.getByName(p2pIpAddr));
                    multicastSocket.joinGroup(new InetSocketAddress(multiGroup,port),itf);
                    multicastSocket.receive(recv);
                    String receiver = new String(recv.getData()).trim();
                    Log.d("来自组主的组内组播消息：：：：",receiver);
                    multicastSocket.close();
                    /**
                     * 如果是符合信息的网关，更新RR，更新cache信息，向外转发。
                     * receiver的格式：[mac1,mac2...]-RR
                     * RR的格式：TTL(String)+MACOFRRN(String)+PathInfo(List(String,String,String...))+TAG(String)+RequestName(String)+RequestType(String)
                     */
                    if(receiver!=null){
                        String[] firstDealMessage = receiver.split("-");
                        String allowedMAC = firstDealMessage[0].replace("[","").replace("]","");
                        allowedMAC.replace(" ","");
                        String[] allwoedMACS = allowedMAC.split(",");
                        for(int i =0; i<allwoedMACS.length;i++){
                            if(allwoedMACS[i].compareTo(macOfThisDevice)==0){
                                /**
                                 * 将收到的组播信息更新A(RR,CacheInformation)发送给LC连接的GO
                                 * 需要的信息：发送端WiFi接口的ip地址，组间组播通信的port号40001，组播地址 239.1.2.4
                                 */
                                String[] RR = firstDealMessage[1].split("\\+");
                                int TTL = Integer.valueOf(RR[0]);
                                TTL = TTL-1;
                                List<String> path = new ArrayList<>();
                                String [] temp = RR[2].split(",");
                                for(int j =0 ; j<temp.length;j++){
                                    path.add(temp[j]);
                                }
                                path.add(goMAC);
                                ResourceRequestPacket resourceRequestPacket = new ResourceRequestPacket(TTL,RR[1],path,RR[3],RR[4],RR[5]);
                                BasicWifiDirectBehavior.getGmCacheInformation().addCacheRecommend(System.currentTimeMillis(),RR[4]);
                                Multicast multicast = new Multicast(40001,"send","239.1.2.4",wifiIP,resourceRequestPacket.toString());
                                multicast.start();
                            }
                        }
                    }
                }
//                Message message = Message.obtain();
//                message.obj = receiver;
//                message.what = 7;
//                messageHandler.sendMessage(message);
//                main2ActivityMessagHandler.sendMessage(message);
                //不需要发给主线程，更新RR信息
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
