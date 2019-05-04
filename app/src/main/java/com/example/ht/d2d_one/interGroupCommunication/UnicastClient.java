package com.example.ht.d2d_one.interGroupCommunication;


import android.os.Message;
import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.communication.ClientSocket;
import com.example.ht.d2d_one.communication.MyMulticastSocketThread;
import com.example.ht.d2d_one.icn.DataRoutingWithOpportunistic;
import com.example.ht.d2d_one.util.FileTransfer;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
//                String message = null;
                 InputStream inputStream = socket.getInputStream();
                 ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                while (true) {
                    //由于bufferedReader.readLine是一个阻塞是方法，所以本方法也是阻塞式的
                    try {
                        if(socket.isClosed()){
                            break;
                        }
                        //处理网关节点和组主节点第一次LC连接以及组主节点表的维持
                        /**
                         * message 的格式：gomac+gwmac
                         * 所以，这里利用发送来的到信息更新LC组主的网关节点表
                         */
                        String headInfo = null;
                        FileTransfer fileTransfer = null;
                        fileTransfer = (FileTransfer) objectInputStream.readObject();
                        headInfo = fileTransfer.getHead();
                        if(firstInfoFromGW){
                            if(headInfo!=null){
                                  Log.d("来自网关节点的单播消息", headInfo);
                            }
                            String[] messages = headInfo.split("\\+");
                            GateWay gateWay = new GateWay(messages[1], messages[0]);
                            //LC组主记录socket信息时，需要用网关节点的mac地址作为唯一标识
                            BasicWifiDirectBehavior.icnOfGO.addInterGroupSocketInfo(messages[1], socket);
                            //添加网关节点到LC组主的GWT表中
                            BasicWifiDirectBehavior.icnOfGO.addGMTable(gateWay, messages[0]);
                            if(messages[1]!=null){
                                Log.d("LC组主添加网关节点信息完成:wlan口MAC", messages[1]);
                            }
                            //开启组内组播，告知本组网关节点本组主所变化的连接信息,通过LC网关节点通知的组主的组内组播使用端口50000,组播地址239.2.1.2 组主连接信息的发送都要加一个info
                            //TODO 注释GOT
//                            String gwMacs = BasicWifiDirectBehavior.icnOfGO.getGWs(BasicWifiDirectBehavior.icnOfGO.getGM(),messages[1]);
//                            String changedGMTContent = MAC+"-"+messages[0]+"-"+"info"+"-"+"gwAdd"+"-"+"0"+gwMacs;
//                            try{
//                                Thread thread = new Thread();
//                                thread.sleep(200);//4000
//                            }catch(InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            MyMulticastSocketThread myMulticastSocketThread = new MyMulticastSocketThread(50000,"send","239.2.1.2",changedGMTContent);
//                            myMulticastSocketThread.start();
//                            String addInfoOne = "TTL-1-1";
//                            try{
//                                Thread thread = new Thread();
//                                thread.sleep(10000);//2000
//                            }catch(InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            String connectInfo = addInfoOne+"-"+BasicWifiDirectBehavior.icnOfGO.getAllGOConInfo(BasicWifiDirectBehavior.icnOfGO.getGM());
//                            MyMulticastSocketThread myMulticastSocketThread1 = new MyMulticastSocketThread(50000,"send","239.2.1.2",connectInfo);
//                            myMulticastSocketThread1.start();
//                            Log.d("LC组主的连接信息","这里是LC组主的连接信息");

                            firstInfoFromGW = false;
                        }else {
                            if(headInfo!=null){
                                String[] messages = headInfo.split("\\+");
                                if(messages.length>2){
                                    if(messages[3].equals("dataBack")){
                                        // 组主节点将数据包发给网关节点。接收的数据信息格式暂为 macOFRRN+pathinfo+resourceName+"databack"+num+hh(data)
                                        //TODO ICN 流表的更新
//                                        String pathInfo = messages[1];
//                                        String [] pathInfos = pathInfo.split(",");
//                                        int num = 0;
//                                        if(pathInfos.length-Integer.parseInt(messages[4])==0){
//                                            String ipOfRRN = GetIpAddrInP2pGroup.getIPFromMac(messages[0]);
//                                            ClientSocket clientSocket = new ClientSocket(ipOfRRN,60006,"write",message);
//                                            clientSocket.start();
//                                        }else{
//                                            String macOfNextHop = pathInfos[pathInfos.length-1-Integer.parseInt(messages[4])].split("-")[1];
//                                            //TODO 如果该网关节点断开，则通过两种路由方式维持信息的传递
//                                            String ipOfNextHop = GetIpAddrInP2pGroup.getIPFromMac(macOfNextHop);
//                                            num = Integer.parseInt(messages[4])+1;
//                                            String content = messages[0]+"+"+messages[1]+"+"+messages[2]+"+"+messages[3]+"+"+String.valueOf(num)+"+"+messages[5];
//                                            ClientSocket clientSocket = new ClientSocket(ipOfNextHop,60006,"write",content);
//                                            clientSocket.start();
//                                        }
                                        if(headInfo!=null){
                                            Log.d("LC组主接收到来自网关的文件信息",headInfo);
                                            String [] headInfos = headInfo.split("\\+");
                                            String []pathInfos = headInfos[1].split(",");
                                            String ipOfRRN = null,ipOfNextHop = null;
                                            //通过资源名称判断是否cache
                                            String filePath = headInfos[2];
                                            filePath.replace(",","").replace(" ","").replace("[","");
                                            String sourceName = filePath.split("/")[filePath.split("/").length-1];
                                            boolean isCache = BasicWifiDirectBehavior.icnOfGO.isCache(sourceName);
                                            if(pathInfos.length-Integer.parseInt(headInfos[4])==0||pathInfos[0].equals("null")){
                                                ipOfRRN = GetIpAddrInP2pGroup.getIPFromMac(headInfos[0]);
                                                //因为是倒数第二跳，所以这样写不会有问题
                                                headInfo = headInfo+"+"+ipOfRRN;
                                                if(ipOfRRN!=null){
                                                    Log.d("ipOfRRN的信息",ipOfRRN);
                                                }
                                                fileTransfer = new FileTransfer(headInfo,fileTransfer.getLength());
                                                if(isCache){
                                                    DataRoutingWithOpportunistic dataRoutingWithOpportunistic = new DataRoutingWithOpportunistic
                                                            (ipOfRRN,60007,"p2pGoToGw",socket,fileTransfer,headInfos[2]);
                                                    //这里出现objectInputStream的原因是上面的线程还未执行完成，inputStream还未读取完成，就开始执行objectInputStream.readObject。
                                                    // 这时候读取的是inputStream中的数据流，不是一个正常的object，所以报错。所以在这个LC组主节点需要
                                                    dataRoutingWithOpportunistic.start();
                                                    while(true){
                                                        if(inputStream.available()==0){
                                                            break;
                                                        }
                                                    }
                                                }else{
                                                    //TODO LC组主第一次成功转发文件时，并没有执行“节点正常接收并转发文件”,为什么？
                                                    //原因在于SocketReuse中没有close Socket，所以这里会阻塞在inputStream.read(buf)，所以方法是通过文件的长度判断文件是否写完。
                                                    Socket socket1 = new Socket(ipOfRRN,60007);
                                                    try{
                                                        OutputStream outputStream = socket1.getOutputStream();
                                                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                                                        objectOutputStream.writeObject(fileTransfer);
                                                        objectOutputStream.flush();
                                                        long total =0;
                                                        int len = 0;
                                                        byte [] buf = new byte[1024*8*512];
                                                        while((len = inputStream.read(buf))!=-1){
                                                            outputStream.write(buf,0,len);
                                                            total+=len;
                                                            if(total >= fileTransfer.getLength()){
                                                                break;
                                                            }
                                                        }
                                                        Log.d("节点正常接收并转发文件",String.valueOf(total));
                                                        outputStream.flush();
                                                        outputStream.close();
                                                        objectOutputStream.close();
                                                    }catch(IOException e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }else{
                                                headInfo = headInfos[0]+"+"+headInfos[1]+"+"+headInfos[2]+"+"+headInfos[3]
                                                        +"+"+String.valueOf(Integer.parseInt(headInfos[4])+1);
                                                String macOfNextHop = pathInfos[pathInfos.length-1-Integer.parseInt(headInfos[4])].split("\\*")[1];
                                                ipOfNextHop = GetIpAddrInP2pGroup.getIPFromMac(macOfNextHop);
                                                headInfo = headInfos[0]+"+"+headInfos[1]+"+"+headInfos[2]+"+"+headInfos[3]
                                                        +"+"+String.valueOf(Integer.parseInt(headInfos[4])+1)+"+"+ipOfNextHop;
                                                if(ipOfNextHop!=null){
                                                    Log.d("ipOfNextHop的信息",ipOfNextHop);
                                                }
                                                fileTransfer = new FileTransfer(headInfo,fileTransfer.getLength());
                                                if(isCache){
                                                    DataRoutingWithOpportunistic dataRoutingWithOpportunistic = new DataRoutingWithOpportunistic
                                                            (ipOfNextHop,60007,"p2pGoToGw",socket,fileTransfer,headInfos[2]);
                                                    dataRoutingWithOpportunistic.start();
                                                }else{
                                                    ClientSocket clientSocket = new ClientSocket(ipOfNextHop,60007,socket,"readANDWrite",fileTransfer,"LC");
                                                    clientSocket.start();
                                                }
                                            }
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
                    }catch (SocketException e) {
                        e.printStackTrace();

                    }catch(ClassNotFoundException e){
                        e.printStackTrace();
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
