package com.example.ht.d2d_one.communication;

import android.os.Handler;
import android.os.Message;
import android.transition.CircularPropagation;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.icn.CacheInformation;
import com.example.ht.d2d_one.icn.DoubleLinkedList;
import com.example.ht.d2d_one.icn.IcnOfNode;
import com.example.ht.d2d_one.icn.LRUCache;
import com.example.ht.d2d_one.icn.MatchingAlgorithm;
import com.example.ht.d2d_one.icn.ResourceRequestPacket;
import com.example.ht.d2d_one.interGroupCommunication.GateWay;
import com.example.ht.d2d_one.interGroupCommunication.MultiCast;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;

import static com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior.messageHandler;

public class MyServerSocketThread implements Runnable{
    public Map<String, String> GMF = new HashMap<>();
    private MyServerSocket myServerSocket = new MyServerSocket();
    private int num = 0;
    public Socket getSocket() {
        return socket;
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    private Socket socket = null;
    private GateWay gateWay = new GateWay(false);

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    //MAC 本机的MAC地址
    private String MAC = null;
    private String label = null;
    private String aimLCGo;
    private int RRTTL = 8;
    private String RRMAC = "";
    private String pathInfo = null;
    private String tag = "";
    private String resourceName = "";
    private String typeOfResourceName = "";
    private ResourceRequestPacket resourceRequestPacket = new ResourceRequestPacket(RRTTL,RRMAC,pathInfo,tag,resourceName,typeOfResourceName);

    //这个content可以从主线程中传下来
    private String content = null;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public MyServerSocketThread(){}
    public MyServerSocketThread(String MAC, Socket socket, String label, int num){
        this.socket = socket;
        this.MAC = MAC;
        this.label = label;
        this.num = num;
    }
    @Override
    public void run() {
        Log.d("MyServerSocket","MyServerSocket开启成功"+num);
        try{
            String clientIpAddrss = socket.getInetAddress().toString();
            clientIpAddrss = clientIpAddrss.substring(1);
            Log.d("此连接的组员设备分配的IP地址为：", ":/-"+clientIpAddrss+label);
            if(label.equals("read")){
                String getResource = read(socket);
                /**
                 * 接收数据类型：true-8-b6:0b:44:c9:26:b9-[]-null-请输入资源-word
                 * 组主接收到查询后，处理RR
                 */
                String[]messageFromClient = getResource.split("\\+");
                /**
                 * 如果第一个部分未字符串true，则表明组主收到的为查询
                 * 首先查询QR表，不论有没有命中，都要记录到QR表中，如果没有命中，就在CS中查看，如果是组内查询，则记录到Ｃache中
                 */
                if(messageFromClient[0].equals("true")){
                    resourceRequestPacket.TTL = Integer.valueOf(messageFromClient[1]);
                    resourceRequestPacket.MACOfRRN = messageFromClient[2];
//                    String[] path = messageFromClient[3].split(",");
//                    for(int i =0;i<path.length;i++){
//                        resourceRequestPacket.PathInfo.add(path[i]);
//                    }
                    resourceRequestPacket.PathInfo = messageFromClient[3];
                    resourceRequestPacket.TAG = messageFromClient[4];
                    resourceRequestPacket.ResourceName = messageFromClient[5];
                    resourceRequestPacket.TypeOfResourceName = messageFromClient[6];
                    Log.d("第一部分的字符串：：",messageFromClient[0]);
                    Log.d("message信息",getResource);
                    boolean isInQR = BasicWifiDirectBehavior.icnOfGO.queryQRTable(resourceRequestPacket);
                    Log.d("查询是否在QR中存在？？",String.valueOf(isInQR));
                    if(!isInQR){
                        if(messageFromClient[5]!=null&&messageFromClient[6]!=null){
                            //资源匹配
                            String qurryName = messageFromClient[5];
                            String qurryType = messageFromClient[6];
                            Log.d("类别标签：::::",qurryType);
                            Log.d("类别：::::",qurryName);
                            String dataToBack;
                            Map<String,String> resultMap = new HashMap<>();
                            switch (qurryType){
                                case "movie":
                                    MatchingAlgorithm matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryMovieMap());
                                    resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryMovieMap());
                                    if(resultMap.size()!=0){
                                        dataToBack = mapToString(resultMap);
                                        if(dataToBack!=null){
                                            Log.d("组主节点处的结果信息",dataToBack);
                                            ClientSocket clientSocketGO  = new ClientSocket(clientIpAddrss,30001,"write",dataToBack);
                                            clientSocketGO.start();
                                            if(messageFromClient[5]!=null&&messageFromClient[6]!=null){
                                                updateCacheTable(messageFromClient[5],messageFromClient[6]);
                                            }
                                        }
                                    }else{
                                        dataToBack = "没有查询到信息";
                                        //更新cacheRecommend,更新RR（TTL减一），组播转发到网关节点（根据最小原则选择网关节点）
                                        if(messageFromClient[5]!=null&&messageFromClient[6]!=null){
                                            updateCacheTable(messageFromClient[5],messageFromClient[6]);
                                        }
                                        intraGroupMulticasatForward();
                                    }
                                    socket.close();
                                    break;
                                case "music":
                                    matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryMusicMap());
                                    resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryMusicMap());
                                    if(resultMap.size()!=0){
                                        dataToBack = mapToString(resultMap);
                                        if(dataToBack!=null){
                                            Log.d("组主节点处的结果信息",dataToBack);
                                            ClientSocket clientSocketGO  = new ClientSocket(clientIpAddrss,30001,"write",dataToBack);
                                            clientSocketGO.start();
                                        }
                                    }else{
                                        dataToBack = "没有查询到信息";
                                        if(messageFromClient[5]!=null&&messageFromClient[6]!=null){
                                            updateCacheTable(messageFromClient[5],messageFromClient[6]);
                                        }
                                        Log.d("到这里了","哈哈哈哈");
                                        intraGroupMulticasatForward();
                                    }
                                    socket.close();
                                    break;
                                case "packet":
                                    matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryPackageMap());
                                    resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryPackageMap());
                                    if(resultMap.size()!=0){
                                        dataToBack = mapToString(resultMap);
                                         if(dataToBack!=null){
                                            Log.d("组主节点处的结果信息",dataToBack);
                                            ClientSocket clientSocketGO  = new ClientSocket(clientIpAddrss,30001,"write",dataToBack);
                                            clientSocketGO.start();
                                        }
                                    }else{
                                        dataToBack = "没有查询到信息";
                                        if(messageFromClient[5]!=null&&messageFromClient[6]!=null){
                                            updateCacheTable(messageFromClient[5],messageFromClient[6]);
                                        }
                                        Log.d("到这里了","哈哈哈哈");
                                        intraGroupMulticasatForward();
                                    }
                                    socket.close();
                                    break;
                                case "word":
                                    matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryWordMap());
                                    resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryWordMap());
                                    if(resultMap.size()!=0){
                                        dataToBack = mapToString(resultMap);
                                        if(dataToBack!=null){
                                            Log.d("组主节点处的结果信息",dataToBack);
                                            ClientSocket clientSocketGO  = new ClientSocket(clientIpAddrss,30001,"write",dataToBack);
                                            clientSocketGO.start();
                                        }
                                    }else{
                                        dataToBack = "没有查询到信息";
                                        if(messageFromClient[5]!=null&&messageFromClient[6]!=null){
                                            updateCacheTable(messageFromClient[5],messageFromClient[6]);
                                        }
                                        Log.d("到这里了","哈哈哈哈");
                                        intraGroupMulticasatForward();
                                    }
                                    socket.close();
                                    break;
                                default:
                                    //对于无此类型的数据，不再转发
                                    Log.d("查询结果","无此查询类型，请重新输入：");
                                    socket.close();
//                                    //更新QR
//                                    BasicWifiDirectBehavior.icnOfGO.addQRTable(resourceRequestPacket);
                            }
                        }
                    }else{
                        Log.d("该查询在QR中命中","该查询不需要查看CS表，只需等待。。。。");
                    }
                }
                /**
                 * 1若第一个部分为toBeGateway，表示组主收到的是组员成网关节点的信息，通过查看网关节点信息表，决定该节点与那个组主建立LC连接
                 * 网关节点列表存放在IcnOfGO的GM中；2、更新网关节点表，并将该新增或者删去的网关节点所带来的组主新连接向本组主的所有网关节点通过
                 * 组播的方式发送出去，如果是网关节点的新增，则带上gwAdd的标记（对于网关节点的离开，不会激发组主节点发送自己的连接信息）。
                 */
                else if(messageFromClient[0].equals("toBeGateway")){
                    // GMF 本组的网关节点信息
                    GMF = BasicWifiDirectBehavior.icnOfGO.getGM();
                    //当前网关节点列表为空，直接将申请成为网关节点的设备成为网关节点，LC GO随机分配/
                    // 或者根据组员携带来的邻近组主的信息比如信号强度
                    String macOfGM = null;
                    if(messageFromClient[1]!=null){
                        macOfGM = messageFromClient[1];
                    }
                    if(messageFromClient[2]!=null){
                        String stringFromPreGW = messageFromClient[2];
                        //将该字符串切割，放到MAp中，调用一个小的推荐函数。具体的方法写到GateWay类中
                        stringFromPreGW = stringFromPreGW.replaceAll("\\[","");
                        stringFromPreGW = stringFromPreGW.replaceAll("\\]","");
                        stringFromPreGW = stringFromPreGW.replaceAll(" ","");
                        aimLCGo = gateWay.chooseWifiGO(GMF,stringFromPreGW,MAC);
                        /**
                         * 再开启一个客户端子线程，将aimLCGo发送给该准网关节点
                         */
                        Log.d("推荐组主",aimLCGo);
                        new ClientSocket(clientIpAddrss,30002,"write",aimLCGo).start();
                        //在这里更新GM表,默认执行到这一步的时候可以正常创建网关节点，如果在组员连接WiFi的时候出现问题，可以再更新GM列表
                        GateWay gateWay = new GateWay(macOfGM,MAC);
                        BasicWifiDirectBehavior.icnOfGO.addGMTable(gateWay,aimLCGo);
                        socket.close();
                        String gwMacs = BasicWifiDirectBehavior.icnOfGO.getGWs(BasicWifiDirectBehavior.icnOfGO.getGM(),macOfGM);
                        String changedGMTContent = MAC+"-"+aimLCGo+"-"+"info"+"-"+"gwAdd"+"-"+"0"+gwMacs;
//                        try{
//                            Thread thread = new Thread();
//                            thread.sleep(15000);//12000
//                        }catch(InterruptedException e){
//                            e.printStackTrace();
//                        }
                        //开启组内组播 通过网关节点通知组主,使用使用组播地址为239.2.1.2，端口号是50000，组主的连接信息的发送都要加一个info在第一个+之后
                        MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"send","239.2.1.2",changedGMTContent);
                        myMulticastSocketThread.start();
                        try{
                            Thread thread = new Thread();
                            thread.sleep(10000);//5000
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                        //开启组播，将自己的连接信息，应该转发的跳数以及剩余TTL转发给所有网关节点（因为这里是开始，没有input的网关节点）
                        // 格式为：TTL-1(应该转发的跳数)-1(当前剩余的跳数)-thisMAC-MAC1-MAC2...起始状态为TTL-1-1-thisMAC-MAC1-MAC2...向所有的网关节点转发
                        String addInfoOne = "TTL-1-1";
                        String connectInfo = addInfoOne+"-"+BasicWifiDirectBehavior.icnOfGO.getAllGOConInfo(BasicWifiDirectBehavior.icnOfGO.getGM());
                        MyMulticastSocketThread myMulticastSocketThread1 = new MyMulticastSocketThread(50000,"send","239.2.1.2",connectInfo);
                        myMulticastSocketThread1.start();
                        Log.d("第一次非变化性消息","coming!!");
                    }
                }else if(messageFromClient[0].equals("leave")){
                    //这里不需要有操作了 2019-3-5
                }
                //组主设备接收来自组员设备的资源名称信息
                else if(messageFromClient[0].equals("source")){
                    Log.d("资源清单:::::::",getResource);
                    //将资源从该子线程发送到主线程中，跨越一层线程
                    Message message = Message.obtain();
                    message.what = 2;
                    message.obj = messageFromClient[1];
                    if(message.obj!=null){
                        Log.d("寻找资源的子线程中message的信息：",message.obj.toString());
                    }
                    //MyServerSocket.handlerMyServerSocket.sendMessage(message);
                    messageHandler.sendMessage(message);
                    socket.close();
                }else if(messageFromClient[3].equals("dataBack")){
                    //TODO 当数据回溯时，需要更新ICN流表
                    String []pathInfos = messageFromClient[1].split(",");
                    String ipOfRRN = null,ipOfNextHop = null;
                    if(pathInfos.length-Integer.parseInt(messageFromClient[4])==0){
                        ipOfRRN = GetIpAddrInP2pGroup.getIPFromMac(messageFromClient[0]);
                        getResource = getResource+"+"+ipOfRRN;
                        if(ipOfRRN!=null){
                            Log.d("ipOfRRN的信息",ipOfRRN);
                        }
                        ClientSocket clientSocket = new ClientSocket(ipOfRRN,60006,"write",getResource);
                        clientSocket.start();
                    }else{
                        getResource = messageFromClient[0]+"+"+messageFromClient[1]+"+"+messageFromClient[2]+"+"+messageFromClient[3]
                                +"+"+String.valueOf(Integer.parseInt(messageFromClient[4])+1)+"+"+messageFromClient[5];
                        String macOfNextHop = pathInfos[pathInfos.length-1-Integer.parseInt(messageFromClient[4])].split("-")[1];
                        ipOfNextHop = GetIpAddrInP2pGroup.getIPFromMac(macOfNextHop);
                        getResource = getResource+"+"+ipOfNextHop;
                        if(ipOfNextHop!=null){
                            Log.d("ipOfNextHop的信息",ipOfNextHop);
                            //F4:F5:DB:01:91:44
                        }
                        ClientSocket clientSocket = new ClientSocket(ipOfNextHop,60006,"write",getResource);
                        clientSocket.start();
                    }
                }
            }else if(label.equals("write")){
                //首先获取资源，字符串类型,然后调用write方法
                write(content);
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /**
     * 循环发送网关节点表的进程，这个过程组播组地址为 239.1.2.7
     * 发送的内容结构为：MACGOA-MACGOB-MACGOC-MACGOD...，这个内容需要对GM进行一步的抽取
     */
    class CircleMultiCastGWTInfo implements Runnable{
        String content;
        public CircleMultiCastGWTInfo(String macOfThisGO){
            content = macOfThisGO;
        }
        @Override
        public void run() {
            try{
                while(true){
                    Collection<String> LCGOs = BasicWifiDirectBehavior.icnOfGO.getGM().values();
                    for(String str:LCGOs){
                        content = content+"-"+str;
                    }
                    MyMulticastSocketThread multicastSocketThread = new MyMulticastSocketThread
                            (40000,"send","239.1.2.7",content);
                    //每隔10s钟组播一次
                    Thread.sleep(10000);
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    /**
     * 这个方法是将资源存放到内存中，不能永久保存，在数据传输中，需要存储到文件或者SD卡中，还需要对应音视频，apk等文件格式
     * @param socket
     * @return
     */
    public String read (Socket socket){
        String content = null;
        try{
            BufferedReader bufferedReader = new BufferedReader(new
                    InputStreamReader(socket.getInputStream()));
            content = bufferedReader.readLine();
            bufferedReader.close();
        }catch (IOException e){
            System.out.print(e);
            //当组员离开，清除服务端维持端socket
            myServerSocket.getSocketList().remove(socket);
        }
        return content;
    }
    public void write(String resource){
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new
                    OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(resource);
            bufferedWriter.close();
        }catch (IOException e){
            System.out.print(e);
            myServerSocket.getSocketList().remove(socket);
        }
    }

    /**
     *
     * @param map map类型不能直接在线程之间进行传输
     * @return String类型便于线程之间的数据传输, 返回值的格式为：mac+path-name*mac+path-name......*
     */
    private String mapToString(Map<String,String> map){
        Set<String> indexs = map.keySet();
        String[] firstPartOfMap = new String[indexs.size()];
        String[] secondPartOfMap = new String[indexs.size()];
        String resultString = null;
        int i =0;
        for(String index:indexs){
            firstPartOfMap[i] = index;
            secondPartOfMap[i] = map.get(index);
        }
        for(i =0;i<indexs.size();i++){
            resultString = firstPartOfMap[i]+"-"+secondPartOfMap[i]+"*";
        }
        return resultString;
    }

    /**
     * 对于组内的查询，不论是否查询成功，都记录到CacheRecommend 中
     * pathinfo 为空表示是本组的查询
     * @param resourcename  查询名称
     * @param resourcetype  查询信息的类型
     */
    private void updateCacheTable(String resourcename,String resourcetype){
        if(resourceRequestPacket.PathInfo == null){
            BasicWifiDirectBehavior.getGoCacheInformation().addCacheRecommend(System.currentTimeMillis(),resourcename+"+"+resourcetype);
        }
    }
    /**
     * 当在组主处理查询没有结果时，更新RR，添加QR信息到本组的QR表，并将RR信息组播发送给网关节点
     */
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
}
