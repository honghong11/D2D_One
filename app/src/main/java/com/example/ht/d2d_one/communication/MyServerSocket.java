package com.example.ht.d2d_one.communication;

import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServerSocket extends Thread{
    private ServerSocket serverSocket;
    private String label;
    private int port;
    private List<Socket> socketList = new ArrayList<Socket>();
    private String MAC = null;

    public List<Socket> getSocketList() {
        return socketList;
    }

    public void setSocketList(List<Socket> socketList) {
        this.socketList = socketList;
    }

    public String getLabel() {
        return label;
    }


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

    public MyServerSocket(String MAC, int port, String label){
        this.MAC = MAC;
        this.label = label;
        this.port = port;
    }
    public void run(){
        try{
        serverSocket = new ServerSocket(port);
            while(true){
                Log.d("MyServer","MyServer is listening...");
                Socket s = serverSocket.accept();
                socketList.add(s);
                //accept之后转入到一个自线程socket中，负责与客户端socket之间到读写交互
                new Thread(new MyServerSocketThread(MAC,s,label)).start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
