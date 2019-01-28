package com.example.ht.d2d_one.interGroupCommunication;


import android.os.Message;
import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import static com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior.messageHandler;

/**
 * UnicsatClient LC组主节点检测到组内有不可达的设备出现（利用WiFi连接的网关节点）,调用一次
 */
public class UnicastClient extends Thread{
    private boolean firstInfoFromGW = true;
    private String host;
    private int port;
    private String label;
    private String content;
    private String macLC;
    public  UnicastClient(String host,int port,String label,String content) {
        this.host = host;
        this.port = port;
        this.label = label;
        this.content = content;
    }

    /**
     * 组间单播客户端，第一次时候，LC组主向网关节点发起TCP请求，并保存该Socket到本LC组主节点。其中，第一个字段是网关节点的mac地址
     */
    @Override
    public void run(){
        try{
            Socket socket = new Socket(host,port);
             if(label.equals("write")){
                write(socket,content);
                socket.setKeepAlive(true);
                    //SocketReuse socketReuse = new SocketReuse(socket);
                    String message = "";
                    while (true) {
                        //由于bufferedReader.readLine是一个阻塞是方法，所以本方法也是阻塞式的
                        try {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            message = bufferedReader.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        /**
                         * message 的格式：gomac+gwmac
                         * 所以，这里利用发送来的到信息更新LC组主的网关节点表
                         */
                        if(firstInfoFromGW){
                            Log.d("来自网关节点的单播消息", message);
                            String[] messages = message.split("\\+");
                            GateWay gateWay = new GateWay(messages[1], messages[0]);
                            //LC组主记录socket信息时，需要用网关节点的mac地址作为唯一标识
                            BasicWifiDirectBehavior.icnOfGO.addInterGroupSocketInfo(messages[1], socket);
                            //添加网关节点到LC组主的GWT表中
                            BasicWifiDirectBehavior.icnOfGO.addGMTable(gateWay, messages[0]);
                            Log.d("LC组主添加网关节点信息完成", messages[1]);
                            firstInfoFromGW = false;
                        }else {
                            String[] messages = message.split("\\+");
                            if(messages[1].equals("leave")){
                                // 网关节点离开时，向LC组主发出离开说明。LC组主在接收到离开说明后，更新ICN流表
                                BasicWifiDirectBehavior.icnOfGO.updateGMTable2(messages[0]);
                            }else if(messages[1].equals("data")){
                                // TODO 网关节点将数据包发给LC组主。LC组主进行处理
                            }else if(messages[0].equals("cs")){
                                // LC组主接收网关节点发送来的资源名称，发到主线程中，添加到RN表结构中
                                Message message1 = Message.obtain();
                                message1.what = 2;
                                message1.obj = messages[1];
                                messageHandler.sendMessage(message1);
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
