package com.example.ht.d2d_one.interGroupCommunication;

import android.util.Log;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Unicast extends Thread{
    private String label;
    private String host = null;
    private String content =null;
    private int port;
    private String wifiInterfaceIP;
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    //应该有读的操作和写的操作,label = "read"读操作，label = "write"写操作
    public Unicast(String host, int port, String wifiInterfaceIP, String content, String label){
        this.wifiInterfaceIP = wifiInterfaceIP;
        this.host = host;
        this.port = port;
        this.content = content;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    };

    public void run(){
        try{
//            Socket socket = new Socket(host,port);
            Socket socket = new Socket();
            InetAddress address = InetAddress.getByName(wifiInterfaceIP);
            socket.bind(new InetSocketAddress(address,50000));
            Log.d("组间单播","绑定WiFi网卡成功");
            socket.connect(new InetSocketAddress(host,port));
            Log.d("组间单播","通过WiFi网卡连接server成功");
            if(label.equals("read")){
                String getContent = read(socket);
                socket.close();
            }else if(label.equals("write")){
                write(socket,content);
                socket.close();
                Log.d("客户端写完毕","客户端写完了");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public String read (Socket socket){
        String content = null;
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            content = bufferedReader.readLine();
            Log.d("客户端读","客户端读取的内容"+content);
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
}
