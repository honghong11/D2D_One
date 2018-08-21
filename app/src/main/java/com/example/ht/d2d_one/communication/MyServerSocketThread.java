package com.example.ht.d2d_one.communication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MyServerSocketThread implements Runnable{
    MyServerSocket myServerSocket = new MyServerSocket();
    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    private Socket socket = null;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    private String MAC = null;
    private String label = null;
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

    public MyServerSocketThread(String MAC, Socket socket, String label){
        this.socket = socket;
        this.MAC = MAC;
        this.label = label;
    }
    @Override
    public void run() {
        Log.d("MyServerSocket","MyServerSocket开启成功");
        try{
            if(label.equals("read")){
                String getResource = read(socket);
                Log.d("资源清单:::::::","设备Mac地址"+MAC+"-----"+getResource);
                //如何将getResource 从该子线程发送到主线程中。
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
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(resource);
            bufferedWriter.close();
        }catch (IOException e){
            System.out.print(e);
            myServerSocket.getSocketList().remove(socket);
        }
    }
}
