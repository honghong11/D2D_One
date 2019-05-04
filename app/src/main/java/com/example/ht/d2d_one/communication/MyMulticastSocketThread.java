package com.example.ht.d2d_one.communication;

import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.icn.ResourceRequestPacket;
import com.example.ht.d2d_one.interGroupCommunication.MultiCast;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MyMulticastSocketThread extends Thread{
    private  String label;
    private int port;
    private String content = "1";
    private String multiAddress;
    private String p2pIpAddr;
    private String wifiIP;
    private String macOfThisDevice;
    private String goMAC;
    private String type;
    private boolean isRRForward = true;
    private String currentMAC;
    private String anotherMAC;
    //interfaceNum =0，使用p2p网卡接收，interfaceNum =1,使用wlan网卡接收
    private int interfaceNum;
    private boolean test = false;

    public MyMulticastSocketThread(int port,String label,String multiAddress,String content){
        this.multiAddress = multiAddress;
        this.port = port;
        this.label = label;
        this.content = content;
    }
    public MyMulticastSocketThread(int port,String label,String multiAddress,String content,String type){
        this.multiAddress = multiAddress;
        this.port = port;
        this.label = label;
        this.content = content;
        this.type =type;
    }
    public MyMulticastSocketThread(String goMAC,String macOfThisDevice,int port,String label,String multiAddress,String ipAddr,String wifiIP, int interfaceNum,boolean isRRForward){
        this.goMAC = goMAC;
        this.macOfThisDevice = macOfThisDevice;
        this.multiAddress = multiAddress;
        this.port = port;
        this.label = label;
        this.p2pIpAddr = ipAddr;
        this.wifiIP = wifiIP;
        this.interfaceNum = interfaceNum;
        this.isRRForward = isRRForward;
    }
    public MyMulticastSocketThread(String goMAC,String currentMAC,String anotherMAC,int port,String label,String multiAddress,int interfaceNum,boolean isRRForward){
        this.goMAC = goMAC;
        this.currentMAC = currentMAC;
        this.anotherMAC = anotherMAC;
        this.multiAddress = multiAddress;
        this.port = port;
        this.label = label;
        this.interfaceNum = interfaceNum;
        this.isRRForward = isRRForward;
    }
    //为测试提供的构造方法
    public MyMulticastSocketThread(int port,String label,String multiAddress,boolean test,String p2pIpAddr){
        this.port = port;
        this.label = label;
        this.multiAddress = multiAddress;
        this.test = test;
        this.p2pIpAddr = p2pIpAddr;
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
                if(type == null){
//                    try{
//                        Thread thread = new Thread();
//                        thread.sleep(6000);
//                    }catch (InterruptedException e){
//                        e.printStackTrace();
//                    }
                    //对于p2p口，直接使用IP地址，因为p2p口连接的比较早，在成为网关之前已经完成连接。
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                        intf = en.nextElement();
                        String iface = intf.getName();
                        if(iface.matches(".*" +"p2p0"+ ".*")||iface.substring(1,2).matches(".*"+"p2"+".*")){
                            break;
                        }
                    }
                   // intf = NetworkInterface.getByInetAddress(InetAddress.getByName(p2pIpAddr));
                }else{
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                        intf = en.nextElement();
                        String iface = intf.getName();
                        if(iface.matches(".*" +"wlan0"+ ".*")){
                            break;
                        }
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
                multicastSocket.close();
            }
            //组内的组播接收，使用p2p网卡，在离开的时候关闭此multicastaSocket
            else if(label.equals("recv")){
                NetworkInterface itf = null;
                if(isRRForward){
                    if(interfaceNum==0){
                        //下面这一行是给测试用的，而在传递RR的过程中，需要使用更下面的方式
//                        itf = NetworkInterface.getByInetAddress(InetAddress.getByName(p2pIpAddr));
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                            itf = en.nextElement();
                            String iface = itf.getName();
                            if(iface.matches(".*" +"p2p0"+ ".*")||iface.matches(".*"+"p2p-wlan0-1"+".*")){
                                break;
                            }
                        }
                        Log.d("开启p2p接收","p2p");
                    }else if(interfaceNum == 1){
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                            itf = en.nextElement();
                            String iface = itf.getName();
                            if(iface.matches(".*" +"wlan0"+ ".*")){
                                break;
                            }
                        }
                        Log.d("开启wlan接收","wlan");
                    }
                }else{
                    if(interfaceNum == 0){
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                            itf = en.nextElement();
                            String iface = itf.getName();
                            if(iface.matches(".*" +"p2p0"+ ".*")||iface.matches(".*"+"p2p-wlan0-1"+".*")){
                                break;
                            }
                        }
                        //itf = NetworkInterface.getByInetAddress(InetAddress.getByName(p2pIpAddr));
                    }else{
                        try{
                            Thread thread = new Thread();
                            thread.sleep(6500);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                            itf = en.nextElement();
                            String iface = itf.getName();
                            if(iface.matches(".*" +"wlan0"+ ".*")){
                                break;
                            }
                        }
                    }
                }
                while(true){
                    MulticastSocket multicastSocket = new MulticastSocket(port);
//                    NetworkInterface itf = null;
                    byte [] buf = new byte[10000];
                    DatagramPacket recv = new DatagramPacket(buf,buf.length);
                    InetAddress multiGroup = createMulticastGroup(multiAddress);
                    multicastSocket.joinGroup(new InetSocketAddress(multiGroup,port),itf);
                    //Log.d("组员设备的IP地址",p2pIpAddr);
                    //multicastSocket.setInterface(InetAddress.getByName(ipAddr));
                    //multicastSocket.joinGroup(multiGroup);
                    multicastSocket.receive(recv);
                    String receiver = new String(recv.getData()).trim();
                    Log.d("myMulticasocket接收的组播消息",receiver);
                    String []strings = receiver.split("-");
                    multicastSocket.close();
                    /**
                     * 如果是符合信息的网关，更新RR，更新cache信息，向外转发。
                     * receiver的格式：[mac1,mac2...]-RR
                     * RR的格式：TTL(String)+MACOFRRN(String)+PathInfo(List(String,String,String...))+TAG(String)+RequestName(String)+RequestType(String)
                     */
                    if(!test){
                        if(receiver!=null){
                            /**
                             * 如果是组主连接变化信息，格式为mac1-mac2-info-gwAdd-0-gwMacs或者mac1-mac2-info-gwLeave-0-gwMacs
                             * 网关节点进行处理
                             * 不论是gwAdd还是gwleave，网关节点操作都一样，判断能否更新ROT表，能则更新并向另外的组主转发，否则，停止该信息的转发
                             * 当接收的地址中没有本设备MAC地址时，本设备不去处理转发该信息
                             * 组主连接信息格式：TTL-应该转发次数-剩余转发次数-thisMAC-mac1-mac2...
                             * 网关节点处理完接收的数据后，对于数据的gwMacs不用发给下一个组主，加上该网关节点的MAC地址，以便下一组主节点选择网关
                             */
                            if(strings.length>2){
                                //TODO 注释GOT
//                                if(strings[2]!=null&&strings[2].equals("info")){
//                                    int a = Integer.parseInt(strings[4]);
//                                    a =a +1;
//                                    receiver = strings[0]+"-"+strings[1]+"-"+strings[2]+"-"+strings[3]+"-"+String.valueOf(a)+"-"+macOfThisDevice;
//                                    int num =0;
//                                    for(int i= 5;i<strings.length;i++){
//                                        if(!macOfThisDevice.equals(strings[i])){
//                                            num++;
//                                        }
//                                    }
//                                    if(num != strings.length-5){
//                                        if(strings[3]!=null){
//                                            /**
//                                             * 如果该变化性信息可以引起组主节点表的变化，更新ROT表，将该信息转发给另一个组主
//                                             * 如果该变化性信息不能引起组主节点表的变化，停止该信息的转发
//                                             */
//                                            if(BasicWifiDirectBehavior.icnOfGW.isChangGOT(BasicWifiDirectBehavior.icnOfGW.getGOT(),receiver)){
//                                                BasicWifiDirectBehavior.icnOfGW.updateGOT(BasicWifiDirectBehavior.icnOfGW.getGOT(),receiver);
//                                                /**
//                                                 * 如果是网关节点的p2p网卡收到的变化性消息，网关节点应该只将该消息转发给wlan网卡连接的组主
//                                                 * 网关节点和LC组主在物理上连接后再发送组播
//                                                 */
//                                                if(interfaceNum == 0){
//                                                    MultiCast multiCast = new MultiCast(50005,"send","239.2.1.3",wifiIP,receiver);
//                                                    multiCast.start();
//                                                }else if(interfaceNum == 1){
//                                                    MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50005,"send","239.2.1.3",receiver);
//                                                    myMulticastSocketThread.start();
//                                                }
//                                            }else{
//                                                Log.d("变化信息不引起ROT表改变，停止转发变化信息",BasicWifiDirectBehavior.icnOfGW.toString(BasicWifiDirectBehavior.icnOfGW.getGOT()));
//                                            }
//                                        }
//                                    }else{
//                                        Log.d("根据接收的消息","不允许从此节点转发");
//                                    }
//                                }else if(strings[0].equals("TTL")){
//                                    //判断组主连接情况是否会引起网关节点组主节点表的变化
//                                    //如果引起变化，则不再转发此连接情况；若不能引起变化，则继续转发,不用判断跳数（跳数的更新在组主节点处理，所以也有组主节点进行判断）
//                                    String conInfos[] = receiver.split("-");
//                                    if(BasicWifiDirectBehavior.icnOfGW.isChangedGOTByConInfo(BasicWifiDirectBehavior.icnOfGW.getGOT(),conInfos)){
//                                        BasicWifiDirectBehavior.icnOfGW.updateGOTByConInfo(BasicWifiDirectBehavior.icnOfGW.getGOT(),conInfos);
//                                        Log.d("GW停止转发组主连接信息：","因为引起了GW组主节点表的更新");
//                                        Log.d("当前组主节点表",BasicWifiDirectBehavior.icnOfGW.toString(BasicWifiDirectBehavior.icnOfGW.getGOT()));
//                                    }else{
//                                        Log.d("GW转发组主连接信息：","因为未引起了GW组主节点表的更新");
//                                        if(interfaceNum == 0){
//                                            MultiCast multiCast = new MultiCast(50005,"send","239.2.1.3",wifiIP,receiver);
//                                            multiCast.start();
//                                        }else if(interfaceNum == 1){
//                                            MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50005,"send","239.2.1.3",receiver);
//                                            myMulticastSocketThread.start();
//                                        }
//                                    }
//                                }
                            }
                            else{
                                //处理转发的查询信息 RR
                                //将查询记录到cache中
                                String[] firstDealMessage = receiver.split("-");
                                String allowedMAC = firstDealMessage[0].replace("[","").replace("]","");
                                allowedMAC.replace(" ","");
                                String[] allwoedMACS = allowedMAC.split(",");
                                for(int i =0; i<allwoedMACS.length;i++){
                                    if(allwoedMACS[i].compareToIgnoreCase(currentMAC)==0){
                                        /**
                                         * 将收到的组播信息更新A(RR,CacheInformation)发送给LC连接的GO
                                         * 需要的信息：发送端WiFi接口的ip地址，组间组播通信的port号40001，组播地址 239.1.2.4
                                         * path的格式：GO-gw,GO-gw,....
                                         */
                                        String[] RR = firstDealMessage[1].split("\\+");
                                        int TTL = Integer.valueOf(RR[0]);
                                        TTL = TTL-1;
                                        String path = null;
                                        if(RR[2]==null||RR[2].equals("null")){
                                            path = goMAC+"*"+anotherMAC;
                                        }else{
                                            path = RR[2]+", "+goMAC+"*"+anotherMAC;
                                        }
                                        ResourceRequestPacket resourceRequestPacket = new ResourceRequestPacket(TTL,RR[1],path,RR[3],RR[4],RR[5]);
                                        if(BasicWifiDirectBehavior.icnOfGW.isAddCache(RR[4]+"-"+RR[1])){
                                            BasicWifiDirectBehavior.icnOfGW.addCache(System.currentTimeMillis(),RR[4]+"-"+RR[1]);
                                        }
                                        Log.d("网关节点中的cache信息",BasicWifiDirectBehavior.icnOfGW.cacheToString());
                                        if(interfaceNum==1){
                                            MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(40001,"send","239.1.2.4",resourceRequestPacket.toString(),null);
                                            myMulticastSocketThread.start();
                                            Log.d("网关节点通过p2p网卡转发RR信息",resourceRequestPacket.toString());
                                        }else if(interfaceNum ==0){
                                            wifiIP = "kk";
                                            MultiCast multicast = new MultiCast(40001,"send","239.1.2.4",wifiIP,resourceRequestPacket.toString());
                                            multicast.start();
                                            Log.d("网关节点通过wlan网卡转发RR信息",resourceRequestPacket.toString());
                                        }
                                    }
                                }
                            }
                        }
                    }
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
    public void close(MyMulticastSocketThread myMulticastSocketThread){
        myMulticastSocketThread.interrupt();
        myMulticastSocketThread = null;
    }
    public void leaveMulticastGroup(){
    }
}
