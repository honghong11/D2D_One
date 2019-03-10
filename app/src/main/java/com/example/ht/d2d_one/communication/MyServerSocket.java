package com.example.ht.d2d_one.communication;

import android.net.wifi.WifiManager;
import android.os.Message;
import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.interGroupCommunication.GateWay;
import com.example.ht.d2d_one.interGroupCommunication.SocketReuse;
import com.example.ht.d2d_one.util.FileTransfer;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;
import com.example.ht.d2d_one.util.WifiAutoConnectManager;
import com.example.ht.d2d_one.util.WifiDeviceWithLabel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.ht.d2d_one.intraGroupCommunication.IntraCommunication.main2ActivityMessagHandler;
import static com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior.messageHandler;

public class MyServerSocket extends Thread{
    private ServerSocket goServerSocket;
    private WifiManager wifiManager;
    private String label;
    private int port;
    private List<Socket> socketList = new ArrayList<Socket>();
    private String MAC = null;
    private String tag;
    private boolean test = false;
    private String goMAC;
    //Gateway对象
    private String deviceMAC;
    private String p2pGOMAC;
    private GateWay gateWay = new GateWay(deviceMAC,p2pGOMAC);
    private String p2pMAC;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    private static List<String> source;

    public List<Socket> getSocketList() {
        return socketList;
    }

    public void setSocketList(List<Socket> socketList) {
        this.socketList = socketList;
    }

    public String getLabel() {
        return label;
    }
    public List<WifiDeviceWithLabel> list = new ArrayList<WifiDeviceWithLabel>();

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    public MyServerSocket(){}

    public MyServerSocket(String goMAC,WifiManager wifiManager,List<WifiDeviceWithLabel> list,String MAC, int port, String label, String tag){
        this.goMAC = goMAC;
        this.wifiManager = wifiManager;
        this.list = list;
        this.MAC = MAC;
        this.port = port;
        this.label = label;
        this.tag = tag;
    }
    //因为许多Icn操作需要在子线程中做，需要IcnOfGO去操作其中的各个表格
    public MyServerSocket(int port,boolean test){
        this.port =port;
        this.test =test;
    }
    public MyServerSocket(int port,String label,String tag){
        this.port = port;
        this.label = label;
        this.tag = tag;
    }
    public MyServerSocket(int port,String label,String tag,String p2pMAC){
        this.port = port;
        this.label = label;
        this.tag = tag;
        this.p2pMAC = p2pMAC;
    }
    public MyServerSocket(String MAC, int port, String label){
        this.MAC = MAC;
        this.port = port;
        this.label = label;
    }
    public MyServerSocket(String MAC, int port, String label,String tag){
        this.MAC = MAC;
        this.label = label;
        this.port = port;
        this.tag = tag;
    }
    public void run(){
        try{
            //这一对socket是为组主ServerSocket提供关闭服务而出现的，对于组员开启的服务端，接收到信息后，我就关闭掉，而对于组主的serverSocket，到disconnect时才关闭
        goServerSocket = new ServerSocket(port);
        if(tag!=null){
            if(tag.equals("client")){
                while(true){
                    Log.d("MyServer","ClientMyServer is listening...");
                    Socket goSocket = goServerSocket.accept();
                    if(label.equals("read")){
                        String resultFromGO = read(goSocket);
                        Message message = Message.obtain();
                        message.what = 3;
                        message.obj = resultFromGO;
                        if(message.obj!=null){
                            Log.d("从组主节点获取的资源查询结果：",message.obj.toString());
                            main2ActivityMessagHandler.sendMessage(message);
                            goSocket.close();
                        }
                    }
                }
            }else if(tag.equals("GW")){
                while (true){
                    Log.d("GMServer","GMServer is listening...");
                    Socket goSocket = goServerSocket.accept();
                    if(label.equals("read")){
                        String aimGO = read(goSocket);
                        String []labelGO = new String[2];
                            Log.d("推荐组主的MAC地址",aimGO);
                        if(aimGO!=null){
                            Log.d("推荐的组主信息：：：：：",aimGO);
                            Log.d("附近信息",aimGO.toString());
                            Message message = Message.obtain();
                            message.what = 5;
                            message.obj = aimGO;
                            messageHandler.sendMessage(message);
                            goSocket.close();
                        }
                        for(int j =0;j<list.size();j++){
                            if(aimGO.equals(list.get(j).getDevice().deviceAddress)){
                                labelGO = list.get(j).getLabel().split("/");
                                break;
                            }
                        }
                        Log.d("SSID",labelGO[1]);
                        Log.d("passPhrase",labelGO[2]);
                        /**
                         * 组员根据组主广播出来的网络名称及密码，通过WiFi于第二个组建立连接
                         * 初始化该网关节点的GOT表
                         */
                        WifiAutoConnectManager wifiAutoConnectManager = new WifiAutoConnectManager(wifiManager);
                        wifiAutoConnectManager.connect(labelGO[1],labelGO[2], WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);
                    }
                }
            }else if(tag.equals("leave")){
                while(true){
                    Socket goSocket = goServerSocket.accept();
                    if(label.equals("read")){
                        String disconnectResult = read(goSocket);
                        Log.d("断开请求反馈",disconnectResult);
                        if(disconnectResult!=null){
                            Message message = Message.obtain();
                            message.what = 6;
                            message.obj = disconnectResult;
                            messageHandler.sendMessage(message);
                            goSocket.close();
                        }
                    }
                }
            }else if(tag.equals("interGW")){
                while (true){
                    Log.d("开启组间单播监听。。。。。","组间单播");
                    Socket goSocket = goServerSocket.accept();
                    try{
                        goSocket.setKeepAlive(true);
                    }catch(SocketException e){
                        e.printStackTrace();
                    }
                    if(label.equals("read")){
                        String messageFromLCGO = read(goSocket);
                        if(messageFromLCGO!=null){
                            Log.d("来自LC组主的单播信息",messageFromLCGO);
                            /**
                             * 保存此socket信息
                             */
                            BasicWifiDirectBehavior.icnOfGW.addInterGroupSocketInfo(messageFromLCGO,goSocket);
                        }
                    }
                }
            }else if(tag.equals("dataBack")){
                while(true){
                    Socket socket = goServerSocket.accept();
                    if(label.equals("read")){
                        String result = read(socket);
                        String [] dataBackInfo = result.split("\\+");
                        int num =0;
                        //开始数据回溯
                        if(dataBackInfo.length>1){
                            if(dataBackInfo[4].equals("beginDataBack")){
                                dataBackInfo[4] = "dataBack";
                                //该节点是RON节点，如果本节点p2pmac地址所对应的ip和socket绑定的ip一致，说明该信息是本节点从p2p网卡接收的
                                String ipOfP2pMAC = GetIpAddrInP2pGroup.getLocalIPAddress();
                                String content = dataBackInfo[0]+"+"+dataBackInfo[1]+"+"+dataBackInfo[2]+"+"+dataBackInfo[4]+"+"+String.valueOf(num)+"+"+"hh";
                                if(ipOfP2pMAC.equals(dataBackInfo[5])){
                                    ClientSocket clientSocket = new ClientSocket("192.168.49.1",30000,"write",content);
                                    clientSocket.start();
                                    Log.d("RON节点接收到组主的命令","RON节点开始数据回溯");
                                    //TODO 使用FileTransfer来传输数据
//                                    FileTransfer fileTransfer = new FileTransfer(content);
//                                    ClientSocket clientSocket1 = new ClientSocket(ipOfRon,60006,"writeFile",fileTransfer);
//                                    clientSocket1.start();
                                }else{
                                    //如果socket绑定的IP和p2pmac地址对应的IP不一样,则说明该节点是通过WiFi和当前组主建立连接的网关节点，应该复用Socket
                                    Socket socket1 = BasicWifiDirectBehavior.icnOfGW.getISI().get("2");
                                    SocketReuse socketReuse = new SocketReuse(socket1,"write",content);
                                    socketReuse.start();
                                    Log.d("RON节点接收到组主的命令","RON节点通过复用socket开始数据回溯");
                                }
                            }else if(dataBackInfo[3].equals("dataBack")){
                                //TODO ICN流表的更新
                                //中间网关节点进行转发，如果是用p2p端接收的消息，则复用socket发送给另一个组主，如果是wlan0口接收的数据，则正常使用socket。
                                String ipOfP2PMAC = GetIpAddrInP2pGroup.getLocalIPAddress();
                                if(p2pMAC.equals(dataBackInfo[0])){
                                    Log.d("RRN节点得到资源",result);
                                }else{
                                    if(!ipOfP2PMAC.equals(dataBackInfo[6])){
                                        ClientSocket clientSocket = new ClientSocket("192.168.49.1",30000,"write",result);
                                        clientSocket.start();
                                        Log.d("中间网关节点转发","中间网关节点转发成功");
                                    }else{
                                        Socket socket1 = BasicWifiDirectBehavior.icnOfGW.getISI().get("2");
                                        SocketReuse socketReuse = new SocketReuse(socket1,"write",result);
                                        socketReuse.start();
                                        Log.d("中间网关节点转发","中间网关节点复用socket转发成功");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }else{
            while(true){
                if(test){
                    Socket socket = goServerSocket.accept();
                    String message = read(socket);
                    if(message!=null){
                        Log.d("组间单播测试信息",message);
                    }
                }else{
                    Socket goSocket = goServerSocket.accept();
                    Log.d("MyServer","GOMyServer is listening...");
                    socketList.add(goSocket);
                    /**
                     * accept之后转入到一个子线程socket中，负责与客户端socket之间到读写交互
                     * 对于组主所开的线程，再加一个icnOfGO字段
                     */
                    new Thread(new MyServerSocketThread(MAC,goSocket,label,socketList.size())).start();
                }
            }
        }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public String read (Socket socket){
        String content = null;
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            content = bufferedReader.readLine();
            bufferedReader.close();
        }catch (IOException e){
            System.out.print(e);
        }
        return content;
    }
    public void write(Socket socket,String resource){
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(resource);
            Log.d("客户端写","客户端写成功");
            bufferedWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
//    public Object readObject(Socket socket){
//        Object object = new Object();
//        try{
//            InputStream inputStream = socket.getInputStream();
//            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
//            object = objectInputStream.readObject();
//        }catch (ClassNotFoundException e1){
//            e1.printStackTrace();
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//        return object;
//    }
    public void close(){
        try{
            if(goServerSocket!=null){
                goServerSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
}
