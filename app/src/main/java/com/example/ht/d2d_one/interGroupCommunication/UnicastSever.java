package com.example.ht.d2d_one.interGroupCommunication;

import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.util.GetIpAddrInP2pGroup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class UnicastSever extends Thread{
    private int port;
    private String label;
    private String gwmac;
    private String gomac;
    private  ServerSocket serverSocket;
    public UnicastSever(String gomac,String gwmac, int port, String label){
        this.gomac = gomac;
        this.gwmac = gwmac;
        this.port = port;
        this.label = label;
    }
    @Override
    public void run(){
        try{
            serverSocket = new ServerSocket(port);
            while(true){
                Log.d("组间单播监听","正在监听.......");
                Socket socket = serverSocket.accept();
                if(label.equals("read")){
                    String message = read(socket);
                    if(message!=null){
                        Log.d("网关接收到的来自LC组主的单播",message);
                    }
                    socket.setKeepAlive(true);
                    //对于网关节点来讲，只连接一个LC组主，所以也就只保存一个LC组主的socket连接，这里用2来标识
                    BasicWifiDirectBehavior.icnOfGW.addInterGroupSocketInfo("2",socket);
                    gwmac = GetIpAddrInP2pGroup.getWlanMac();
                    Log.d("网关节点的wlan0口mac地址",gwmac);

                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bufferedWriter.write(gomac+"+"+gwmac);
                    bufferedWriter.write('\n');
                    bufferedWriter.write("\n");
                    bufferedWriter.flush();
                    //不再监听TCP请求，而是开一个socketReuse.read
                    String info = "";
                    while (true){
                        try{
                            if(socket.isClosed()){
                                break;
                            }
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            info = bufferedReader.readLine();
                            //Log.d("收到来自LC组主节点的单播消息2",info);
                            if(info.equals("data")){
                                // TODO: 2019/1/21 关于数据包回溯的工作 组主到网关之间的数据传输不用复用socket了，可以正常使用socket
                            }
                        }catch (SocketException e){
                            System.out.print("UnicastServer异常");
                            e.printStackTrace();
                        }
                    }
                }
                break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    private String read(Socket socket){
        String message = "";
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            message = bufferedReader.readLine();
            //bufferedReader.read()
        }catch (IOException e){
            e.printStackTrace();
        }
        return message;
    }
}
