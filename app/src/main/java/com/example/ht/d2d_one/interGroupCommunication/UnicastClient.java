package com.example.ht.d2d_one.interGroupCommunication;


import android.os.Message;
import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.communication.ClientSocket;
import com.example.ht.d2d_one.communication.MyMulticastSocketThread;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

import static com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior.messageHandler;

/**
 * UnicsatClient LC组主节点检测到组内有不可达的设备出现（利用WiFi连接的网关节点）,调用一次
 */
public class UnicastClient extends Thread{
    private boolean firstInfoFromGW = true;
    private String host;
    private int port;
    private String label;
    private String MAC;
    private String macLC;
    public  UnicastClient(String host,int port,String label,String content) {
        this.host = host;
        this.port = port;
        this.label = label;
        this.MAC = content;
    }

    /**
     * 组间单播客户端，第一次时候，LC组主向网关节点发起TCP请求，并保存该Socket到本LC组主节点。其中，第一个字段是网关节点的mac地址
     */
    @Override
    public void run(){
        try{
            Socket socket = new Socket(host,port);
             if(label.equals("write")){
                write(socket,MAC);
                socket.setKeepAlive(true);
                    //SocketReuse socketReuse = new SocketReuse(socket);
                    String message = "";
                    while (true) {
                        //由于bufferedReader.readLine是一个阻塞是方法，所以本方法也是阻塞式的
                        try {
                            if(socket.isClosed()){
                                break;
                            }
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            message = bufferedReader.readLine();
                        } catch (SocketException e) {
                            e.printStackTrace();
                        }
                        /**
                         * message 的格式：gomac+gwmac
                         * 所以，这里利用发送来的到信息更新LC组主的网关节点表
                         */
                        if(firstInfoFromGW){
                            if(message!=null){
                                Log.d("来自网关节点的单播消息", message);
                            }
                            String[] messages = message.split("\\+");
                            GateWay gateWay = new GateWay(messages[1], messages[0]);
                            //LC组主记录socket信息时，需要用网关节点的mac地址作为唯一标识
                            BasicWifiDirectBehavior.icnOfGO.addInterGroupSocketInfo(messages[1], socket);
                            //添加网关节点到LC组主的GWT表中
                            BasicWifiDirectBehavior.icnOfGO.addGMTable(gateWay, messages[0]);
                            if(messages[1]!=null){
                                Log.d("LC组主添加网关节点信息完成", messages[1]);
                            }
                            //开启组内组播，告知本组网关节点本组主所变化的连接信息,通过LC网关节点通知的组主的组内组播使用端口50000,组播地址239.2.1.2 组主连接信息的发送都要加一个info
                            String gwMacs = BasicWifiDirectBehavior.icnOfGO.getGWs(BasicWifiDirectBehavior.icnOfGO.getGM(),messages[1]);
                            String changedGMTContent = MAC+"-"+messages[0]+"-"+"info"+"-"+"gwAdd"+"-"+"0"+gwMacs;
                            try{
                                Thread thread = new Thread();
                                thread.sleep(200);//4000
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                            MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"send","239.2.1.2",changedGMTContent);
                            myMulticastSocketThread.start();
                            String addInfoOne = "TTL-1-1";
                            try{
                                Thread thread = new Thread();
                                thread.sleep(10000);//2000
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                            String connectInfo = addInfoOne+"-"+BasicWifiDirectBehavior.icnOfGO.getAllGOConInfo(BasicWifiDirectBehavior.icnOfGO.getGM());
                            MyMulticastSocketThread myMulticastSocketThread1 = new MyMulticastSocketThread(50000,"send","239.2.1.2",connectInfo);
                            myMulticastSocketThread1.start();
                            Log.d("LC组主的连接信息","这里是LC组主的连接信息");
                            firstInfoFromGW = false;
                        }else {
                            if(message!=null){
                                String[] messages = message.split("\\+");
                                if(messages.length>2){
                                    if(messages[3].equals("dataBack")){
                                        // 组主节点将数据包发给网关节点。接收的数据信息格式暂为 macOFRRN+pathinfo+resourceName+"databack"+num+hh(data)
                                        //TODO ICN 流表的更新
                                        String pathInfo = messages[1];
                                        String [] pathInfos = pathInfo.split(",");
                                        int num = 0;
                                        if(pathInfos.length-Integer.parseInt(messages[4])==0){
                                            String ipOfRRN = GetIpAddrInP2pGroup.getIPFromMac(messages[0]);
                                            ClientSocket clientSocket = new ClientSocket(ipOfRRN,60006,"write",message);
                                            clientSocket.start();
                                        }else{
                                            String macOfNextHop = pathInfos[pathInfos.length-1-Integer.parseInt(messages[4])].split("-")[1];
                                            //TODO 如果该网关节点断开，则通过两种路由方式维持信息的传递
                                            String ipOfNextHop = GetIpAddrInP2pGroup.getIPFromMac(macOfNextHop);
                                            num = Integer.parseInt(messages[4])+1;
                                            String content = messages[0]+"+"+messages[1]+"+"+messages[2]+"+"+messages[3]+"+"+String.valueOf(num)+"+"+messages[5];
                                            ClientSocket clientSocket = new ClientSocket(ipOfNextHop,60006,"write",content);
                                            clientSocket.start();
                                        }
                                    }
                                } else if(messages[0].equals("cs")){
                                    // LC组主接收网关节点发送来的资源名称，发到主线程中，添加到RN表结构中
                                    Message message1 = Message.obtain();
                                    message1.what = 2;
                                    message1.obj = messages[1];
                                    messageHandler.sendMessage(message1);
                                }
                            }
                        }
                    }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //对于read或者write操作，不关闭于socket相关的流
    private String read(Socket socket){
        String message = "";
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            message = bufferedReader.readLine();
        }catch (IOException e){
            e.printStackTrace();
        }
        return  message;
    }
    public void write(Socket socket, String content){
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(content);
            //bufferWriter.readline()是一个阻塞方法，如果没有读到换行符，或者结束符，不会结束readline,我又不能close，所以在发送内容字段后，发送换行符。
            bufferedWriter.write('\n');
            bufferedWriter.write("\n");

//            bufferedWriter.close();
            bufferedWriter.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
