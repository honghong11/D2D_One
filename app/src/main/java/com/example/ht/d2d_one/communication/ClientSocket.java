package com.example.ht.d2d_one.communication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientSocket extends Thread {
    private  Socket socket;
    private String label;
    private String host = null;
    private String content =null;
    private int port;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    //应该有读的操作和写的操作,label = "read"读操作，label = "write"写操作
    public ClientSocket(String host,int port,String label,String content){
        this.host = host;
        this.port = port;
        this.label = label;
        this.content = content;

    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void run(){
        try{
            socket = new Socket(host,port);
            if(label.equals("read")){
                //子线程中获取到数据，需要传给主线程。
                String getContent = read(socket);
                socket.close();
            }else if(label.equals("write")){
                //首先获取资源，字符串类型,然后调用write方法
                write(content);
                socket.close();
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
    public void write(String resource){
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(resource);
            Log.d("客户端写","客户端写成功");
            bufferedWriter.close();
        }catch (IOException e){
            System.out.print(e);
        }
    }
}
