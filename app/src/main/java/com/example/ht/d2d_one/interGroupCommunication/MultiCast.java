package com.example.ht.d2d_one.interGroupCommunication;

import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.communication.ClientSocket;
import com.example.ht.d2d_one.communication.MyMulticastSocketThread;
import com.example.ht.d2d_one.communication.MyServerSocketThread;
import com.example.ht.d2d_one.icn.MatchingAlgorithm;
import com.example.ht.d2d_one.icn.ResourceRequestPacket;
import com.example.ht.d2d_one.util.FileTransfer;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior.messageHandler;

public class MultiCast extends Thread{
    private  String label;
    private int port;
    private String content = "1";
    private String multiAddress;
    private String ipAddr;
    private String goMAC;
    private int TTL;
    private String RRNMAC;
    private String pathInfo;
    private String tag;
    private String qurryName;
    private String typeOfResourceName;
    ResourceRequestPacket resourceRequestPacket = new ResourceRequestPacket(TTL,RRNMAC,pathInfo,tag,qurryName,typeOfResourceName);
    public MultiCast(int port, String label, String multiAddress,String goMAC){
        this.multiAddress = multiAddress;
        this.port = port;
        this.label = label;
        this.goMAC = goMAC;
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
                if(ipAddr.equals("test")){
                    multicastSocket.joinGroup(multiGroup);
                    DatagramPacket datagramPacket = new DatagramPacket(content.getBytes(),content.length(),multiGroup,port);
                    multicastSocket.send(datagramPacket);
                    Log.d("不绑定网卡组播，组播内容：",content);
                    multicastSocket.close();
                }else{
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
                    Log.d("组间广播内容",content);
                    multicastSocket.close();
                }
            }
            else if(label.equals("recv")){
                while(true){
                    MulticastSocket multicastSocket = new MulticastSocket(port);
                    byte [] buf = new byte[3000];
                    DatagramPacket recv = new DatagramPacket(buf,buf.length);
                    InetAddress multiGroup = createMulticastGroup(multiAddress);
                    NetworkInterface itf = NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.49.1"));
                    multicastSocket.joinGroup(new InetSocketAddress(multiGroup,port),itf);
                    Log.d("停在这里啦？","是的，停到这里啦");
                    multicastSocket.receive(recv);
                    String receiver = new String(recv.getData()).trim();
                    Log.d("来自网关的组间组播消息：：：：",receiver);
                    multicastSocket.close();
                    //组主处理接收到的信息
                    //组主连接信息格式：起始状态为TTL"-"1"-"1"-"thisMAC"-"MAC1"-"MAC2...(非变化性连接信息)
                    // 以及MAC+"-"+aimLCGo+"-"+"info"+"-"+"gwAdd"+"-"+0+"-"+MAC（变化性连接信息）
                    //如果是变化性消息，选择接收信息的网关节点,并激发组主开启连接信息转发，根据变化性消息中携带的跳数判断自己的连接信息需要转发多少跳，非变化性消息从所有网关节点转发
                    //如果是非变化性消息 ，根据该消息，判断该消息是否还需要转发
                    String[] connectInfos = receiver.split("-");
                    if(connectInfos.length>2){
                        //TODO 注释GOT
//                        if(connectInfos[2].equals("info")){
//                            String gwmacs = BasicWifiDirectBehavior.icnOfGO.getGWs(BasicWifiDirectBehavior.icnOfGO.getGM(),connectInfos[5]);
//                            connectInfos[4] = String.valueOf(Integer.parseInt(connectInfos[4])+1);
//                            receiver = connectInfos[0]+"-"+connectInfos[1]+"-"+connectInfos[2]+"-"+connectInfos[3]+"-"+connectInfos[4]+"-"+gwmacs;
//                            if(connectInfos[3].equals("gwAdd")){
//                                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"send","239.2.1.2",receiver);
//                                myMulticastSocketThread.start();
//                                //激发组主转发连接信息
//                                int needHops = Integer.parseInt(connectInfos[4]);
//                                String connectInfo = "TTL"+"-"+String.valueOf(needHops)+"-"+String.valueOf(needHops)+"-"+BasicWifiDirectBehavior.icnOfGO.getAllGOConInfo(BasicWifiDirectBehavior.icnOfGO.getGM());
//                                MyMulticastSocketThread myMulticastSocketThread1 = new MyMulticastSocketThread(50000,"send","239.2.1.2",connectInfo);
//                                myMulticastSocketThread1.start();
//                            }else if(connectInfos[3].equals("gwLeave")){
//                                MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"send","239.2.1.2",receiver);
//                                myMulticastSocketThread.start();
//                            }
//                        }else if(connectInfos[0].equals("TTL")){
//                            int ttl = Integer.parseInt(connectInfos[2]);
//                            if(ttl>0){
//                                ttl = ttl -1;
//                                String unChangeContent ="TTL"+"-"+connectInfos[1]+"-"+String.valueOf(ttl);
//                                for(int i =3;i<connectInfos.length;i++){
//                                    unChangeContent = unChangeContent + "-"+connectInfos[i];
//                                }
//                                MyMulticastSocketThread myMulticastSocketThread1 = new MyMulticastSocketThread(50000,"send","239.2.1.2",unChangeContent);
//                                myMulticastSocketThread1.start();
//                            }
//                        }
                    }
                    // TODO 该组主对查询信息进行处理，判断本组是否有查询信息，如果有，开始数据回溯，如果没有，再次转发给对应的网关节点。
                    //TODO 数据回溯，这里应该有两个类（rout1和rout2）去完成
                    String [] messageOfQuery = receiver.split("\\+");
                    if(messageOfQuery.length>1){
                        int RRTTL = Integer.parseInt(messageOfQuery[0]);
                        String RRMAC = messageOfQuery[1];
                        String pathInfo = messageOfQuery[2];
                        String tag = messageOfQuery[3];
                        String qurryName = messageOfQuery[4];
                        String typeOfResourceName = messageOfQuery[5];
                        resourceRequestPacket.TTL = RRTTL;
                        resourceRequestPacket.MACOfRRN = RRMAC;
                        resourceRequestPacket.PathInfo = pathInfo;
                        resourceRequestPacket.TAG = tag;
                        resourceRequestPacket.ResourceName = qurryName;
                        resourceRequestPacket.TypeOfResourceName = typeOfResourceName;
                        //更新cache表，在处理QR表之前
                        if(BasicWifiDirectBehavior.icnOfGO.isAddCache(qurryName+"-"+RRMAC)){
                            BasicWifiDirectBehavior.icnOfGO.addCache(System.currentTimeMillis(),qurryName+"-"+RRMAC);
                        }
                        Log.d("组主节点中的cache信息",BasicWifiDirectBehavior.icnOfGO.cacheToString());
                        boolean isInQR = BasicWifiDirectBehavior.icnOfGO.queryQRTable(resourceRequestPacket);
                        Log.d("查询是否在QR中存在？？",String.valueOf(isInQR));
                        //QR避免了查询环路的出现
                        //TODO 这里避免环路可以在发送兴趣包节点做一次判断来避免
                        Map<String,String> resultMap = new HashMap<>();
                        if(!isInQR){
                            switch (typeOfResourceName){
                                   case "movie":
                                    MatchingAlgorithm matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryMovieMap());
                                    resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryMovieMap());
                                    /**
                                     * 当查询信息匹配，组主的操作，同样的操作应该在MyServerSocketThread以及MyServerSocket中网关节点服务中实现
                                     * 1、根据RON节点的MAC地址，从ARP表中得到其IP地址
                                     * 2、将(RRNMAC+path+name+pathOfResource+flag)发送给RON节点(TCP单播)其中path的格式： gomac-gwmac,gomac-gwmac.....
                                     */
                                    //这里假定一个组内没有重复的资源信息。。。
                                    if(resultMap.size()!=0){
                                        Collection<String> macOfRONs = resultMap.keySet();
                                        String macAndPath = null;
                                        for(String str:macOfRONs){
                                            macAndPath = str;
                                        }
                                        String macOfRON = macAndPath.split("\\+")[0];
                                        String ipOfRon = GetIpAddrInP2pGroup.getIPFromMac(macAndPath.split("\\+")[0]);
                                        String pathOfResource = macAndPath.split("\\+")[1];
                                        pathOfResource = pathOfResource.replace("[","").replace("]","");
                                        String headOfData = resourceRequestPacket.MACOfRRN+"+"+resourceRequestPacket.PathInfo+"+"+
                                                resourceRequestPacket.ResourceName+"+"+pathOfResource+"+"+"beginDataBack"+"+"+ipOfRon;
                                        if(macOfRON.equals(goMAC)){
                                            //TODO 资源在组主处的处理情况 2019-3-27
                                            //处理信息，开启数据的回溯，将文件信息传递给下一跳
                                            //这里num的初值设为1，若是组员节点则是设置为0
                                            String []pathInfos = resourceRequestPacket.PathInfo.split(",");
                                            String macOfNextHop = pathInfos[pathInfos.length-1].split("\\*")[1];
                                            String ipOfNextHop = GetIpAddrInP2pGroup.getIPFromMac(macOfNextHop);
                                            String info = resourceRequestPacket.MACOfRRN+"+"+resourceRequestPacket.PathInfo+"+"+pathOfResource+
                                                    "+"+"dataBack"+"+"+String.valueOf(1)+"+"+ipOfNextHop;
                                            FileTransfer fileTransfer = new FileTransfer(info);
                                            String filePath = fileTransfer.getFilePath(fileTransfer.getHead());
                                            File file = new File(filePath);
                                            long fileLength = file.length();
                                            FileTransfer fileTransfer1 = new FileTransfer(info,fileLength);
                                            ClientSocket clientSocket = new ClientSocket(ipOfNextHop,60007,"writeFile",fileTransfer1);
                                            clientSocket.start();
                                        }else {
                                            ClientSocket clientSocket = new ClientSocket(ipOfRon,60006,"write",headOfData);
                                            clientSocket.start();
                                            Log.d("找到RON节点",ipOfRon);
                                        }
                                    }else{
                                        //组播转发到网关节点（根据最小原则选择网关节点）
                                        intraGroupMulticasatForward();
                                    }
                                    break;
                                case "music":
                                    matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryMusicMap());
                                    resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryMusicMap());
                                    if(resultMap.size()!=0){
                                        Collection<String> macOfRONs = resultMap.keySet();
                                        String macAndPath = null;
                                        for(String str:macOfRONs){
                                            macAndPath = str;
                                        }
                                        String macOfRON = macAndPath.split("\\+")[0];
                                        String ipOfRon = GetIpAddrInP2pGroup.getIPFromMac(macAndPath.split("\\+")[0]);
                                        String pathOfResource = macAndPath.split("\\+")[1];
                                        pathOfResource = pathOfResource.replace("[","").replace("]","");
                                        String headOfData = resourceRequestPacket.MACOfRRN+"+"+resourceRequestPacket.PathInfo+"+"+
                                                resourceRequestPacket.ResourceName+"+"+pathOfResource+"+"+"beginDataBack"+"+"+ipOfRon;
                                        if(macOfRON.equals(goMAC)){
                                            //TODO 资源在组主处的处理情况 2019-3-27
                                            //处理信息，开启数据的回溯，将文件信息传递给下一跳
                                            //这里num的初值设为1，若是组员节点则是设置为0
                                            String []pathInfos = resourceRequestPacket.PathInfo.split(",");
                                            String macOfNextHop = pathInfos[pathInfos.length-1].split("\\*")[1];
                                            String ipOfNextHop = GetIpAddrInP2pGroup.getIPFromMac(macOfNextHop);
                                            String info = resourceRequestPacket.MACOfRRN+"+"+resourceRequestPacket.PathInfo+"+"+pathOfResource+
                                                    "+"+"dataBack"+"+"+String.valueOf(1)+"+"+ipOfNextHop;
                                            FileTransfer fileTransfer = new FileTransfer(info);
                                            String filePath = fileTransfer.getFilePath(fileTransfer.getHead());
                                            File file = new File(filePath);
                                            long fileLength = file.length();
                                            FileTransfer fileTransfer1 = new FileTransfer(info,fileLength);
                                            ClientSocket clientSocket = new ClientSocket(ipOfNextHop,60007,"writeFile",fileTransfer1);
                                            clientSocket.start();
                                        }else {
                                            ClientSocket clientSocket = new ClientSocket(ipOfRon,60006,"write",headOfData);
                                            clientSocket.start();
                                            Log.d("找到RON节点",ipOfRon);
                                        }
                                    }else{
                                        //组播转发到网关节点（根据最小原则选择网关节点）
                                        intraGroupMulticasatForward();
                                    }
                                    break;
                                case "packet":
                                    matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryPackageMap());
                                    resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryPackageMap());
                                    if(resultMap.size()!=0){
                                        Collection<String> macOfRONs = resultMap.keySet();
                                        String macAndPath = null;
                                        for(String str:macOfRONs){
                                            macAndPath = str;
                                        }
                                        String macOfRON = macAndPath.split("\\+")[0];
                                        String ipOfRon = GetIpAddrInP2pGroup.getIPFromMac(macAndPath.split("\\+")[0]);
                                        String pathOfResource = macAndPath.split("\\+")[1];
                                        pathOfResource = pathOfResource.replace("[","").replace("]","");
                                        String headOfData = resourceRequestPacket.MACOfRRN+"+"+resourceRequestPacket.PathInfo+"+"+
                                                resourceRequestPacket.ResourceName+"+"+pathOfResource+"+"+"beginDataBack"+"+"+ipOfRon;
                                        if(macOfRON.equals(goMAC)){
                                            //TODO 资源在组主处的处理情况 2019-3-27
                                            //处理信息，开启数据的回溯，将文件信息传递给下一跳
                                            //这里num的初值设为1，若是组员节点则是设置为0
                                            String []pathInfos = resourceRequestPacket.PathInfo.split(",");
                                            String macOfNextHop = pathInfos[pathInfos.length-1].split("\\*")[1];
                                            String ipOfNextHop = GetIpAddrInP2pGroup.getIPFromMac(macOfNextHop);
                                            String info = resourceRequestPacket.MACOfRRN+"+"+resourceRequestPacket.PathInfo+"+"+pathOfResource+
                                                    "+"+"dataBack"+"+"+String.valueOf(1)+"+"+ipOfNextHop;
                                            FileTransfer fileTransfer = new FileTransfer(info);
                                            String filePath = fileTransfer.getFilePath(fileTransfer.getHead());
                                            File file = new File(filePath);
                                            long fileLength = file.length();
                                            FileTransfer fileTransfer1 = new FileTransfer(info,fileLength);
                                            ClientSocket clientSocket = new ClientSocket(ipOfNextHop,60007,"writeFile",fileTransfer1);
                                            clientSocket.start();
                                        }else {
                                            ClientSocket clientSocket = new ClientSocket(ipOfRon,60006,"write",headOfData);
                                            clientSocket.start();
                                            Log.d("找到RON节点",ipOfRon);
                                        }
                                    }else{
                                        //组播转发到网关节点（根据最小原则选择网关节点）
                                        intraGroupMulticasatForward();
                                    }
                                    break;
                                case "word":
                                    matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryWordMap());
                                    resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryWordMap());
                                    if(resultMap.size()!=0){
                                        Collection<String> macOfRONs = resultMap.keySet();
                                        String macAndPath = null;
                                        for(String str:macOfRONs){
                                            macAndPath = str;
                                        }
                                        String macOfRON = macAndPath.split("\\+")[0];
                                        String ipOfRon = GetIpAddrInP2pGroup.getIPFromMac(macAndPath.split("\\+")[0]);
                                        String pathOfResource = macAndPath.split("\\+")[1];
                                        pathOfResource = pathOfResource.replace("[","").replace("]","");
                                        String headOfData = resourceRequestPacket.MACOfRRN+"+"+resourceRequestPacket.PathInfo+"+"+
                                                resourceRequestPacket.ResourceName+"+"+pathOfResource+"+"+"beginDataBack"+"+"+ipOfRon;
                                        if(macOfRON.equals(goMAC)){
                                            //TODO 资源在组主处的处理情况 2019-3-27
                                            //处理信息，开启数据的回溯，将文件信息传递给下一跳
                                            //这里num的初值设为1，若是组员节点则是设置为0
                                            String []pathInfos = resourceRequestPacket.PathInfo.split(",");
                                            String macOfNextHop = pathInfos[pathInfos.length-1].split("\\*")[1];
                                            String ipOfNextHop = GetIpAddrInP2pGroup.getIPFromMac(macOfNextHop);
                                            String info = resourceRequestPacket.MACOfRRN+"+"+resourceRequestPacket.PathInfo+"+"+pathOfResource+
                                                    "+"+"dataBack"+"+"+String.valueOf(1)+"+"+ipOfNextHop;
                                            FileTransfer fileTransfer = new FileTransfer(info);
                                            String filePath = fileTransfer.getFilePath(fileTransfer.getHead());
                                            File file = new File(filePath);
                                            long fileLength = file.length();
                                            FileTransfer fileTransfer1 = new FileTransfer(info,fileLength);
                                            ClientSocket clientSocket = new ClientSocket(ipOfNextHop,60007,"writeFile",fileTransfer1);
                                            clientSocket.start();
                                        }else {
                                            ClientSocket clientSocket = new ClientSocket(ipOfRon,60006,"write",headOfData);
                                            clientSocket.start();
                                            Log.d("找到RON节点",ipOfRon);
                                        }
                                    }else{
                                        //组播转发到网关节点（根据最小原则选择网关节点）
                                        intraGroupMulticasatForward();
                                    }
                                    break;
                                default:
                                    //对于无此类型的数据，不再转发
                                    //TODO 更新QR表
                                    Log.d("查询结果","无此查询类型，请重新输入：");
                            }
                        }
                    }
                    //TODO 计算TTL返回所需要的时间，在这个时间限后还没有数据返回就认为未查找到资源
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
    private void intraGroupMulticasatForward(){
        resourceRequestPacket.TTL = resourceRequestPacket.TTL - 1;
        BasicWifiDirectBehavior.icnOfGO.addQRTable(resourceRequestPacket);
        List<String> gwNodes;
        gwNodes = BasicWifiDirectBehavior.icnOfGO.chooseGW(BasicWifiDirectBehavior.icnOfGO.getGM());
        String temp = gwNodes.toString().replace("\\[","").replace("\\]","");
        String nodeInfo = temp.replace(" ","");
        MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread
                (40000,"send","239.1.2.3",nodeInfo+"-"+resourceRequestPacket.toString());
        myMulticastSocketThread.start();
        Log.d("组播发送信息",nodeInfo+"-"+resourceRequestPacket.toString());
    }
    //更新缓存
//    private void updateCacheTable(String resourcename,String resourcetype){
//        if(resourceRequestPacket.PathInfo == null){
//            BasicWifiDirectBehavior.icnOfGO.addCache(System.currentTimeMillis(),resourcename+"+"+resourcetype);
//        }
//    }
}

