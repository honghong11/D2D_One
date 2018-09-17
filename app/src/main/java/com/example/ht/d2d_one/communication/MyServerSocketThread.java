package com.example.ht.d2d_one.communication;

import android.os.Message;
import android.util.Log;

import com.example.ht.d2d_one.DeviceListFragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MyServerSocketThread implements Runnable{
    MyServerSocket myServerSocket = new MyServerSocket();
    private int MRESOURCE = 2;
    private int num = 0;
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

    public MyServerSocketThread(String MAC, Socket socket, String label,int num){
        this.socket = socket;
        this.MAC = MAC;
        this.label = label;
        this.num = num;
    }
    @Override
    public void run() {
        Log.d("MyServerSocket","MyServerSocket开启成功"+num);
        try{
            if(label.equals("read")){
                String getResource = read(socket);
                Log.d("资源清单:::::::",getResource);
                //将资源从该子线程发送到主线程中，跨越一层线程
                Message message = Message.obtain();
                message.what = MRESOURCE;
                message.obj = getResource;
                if(message.obj!=null){
                    Log.d("寻找资源的子线程中message的信息：",message.obj.toString());
                }
                //MyServerSocket.handlerMyServerSocket.sendMessage(message);
                DeviceListFragment.messageHandler.sendMessage(message);
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
