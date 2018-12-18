package com.example.ht.d2d_one.communication;

import android.net.wifi.WifiManager;
import android.os.Message;
import android.util.Log;

import com.example.ht.d2d_one.interGroupCommunication.GateWay;
import com.example.ht.d2d_one.util.WifiAutoConnectManager;
import com.example.ht.d2d_one.util.WifiDeviceWithLabel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
    //Gateway对象
    private String deviceMAC;
    private String p2pGOMAC;
    private GateWay gateWay = new GateWay(deviceMAC,p2pGOMAC);

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

    public MyServerSocket(WifiManager wifiManager,List<WifiDeviceWithLabel> list,String MAC, int port, String label, String tag){
        this.wifiManager = wifiManager;
        this.list = list;
        this.MAC = MAC;
        this.port = port;
        this.label = label;
        this.tag = tag;
    }
    //因为许多Icn操作需要在子线程中做，需要IcnOfGO去操作其中的各个表格
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
                         */
                        WifiAutoConnectManager wifiAutoConnectManager = new WifiAutoConnectManager(wifiManager);
                        wifiAutoConnectManager.connect(labelGO[1],labelGO[2], WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);
                        //当连接失败，应该告诉组主
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
            }
        }else{
            while(true){
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
