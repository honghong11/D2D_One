package com.example.ht.d2d_one.communication;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static com.example.ht.d2d_one.Main2Activity.main2ActivityMessagHandler;

public class ClientSocket extends Thread {
    private  Socket socket;
    private String label;
    private String host = null;
    private String content =null;
    final private int QURRYFROMGO =3;
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
                Log.d("客户端写完毕","客户端写完了");
            }else if(label.equals("qurry")){
                qurry(true,content);
//                String resultFromGO = read(socket);
//                Message message = Message.obtain();
//                message.what = QURRYFROMGO;
//                message.obj = resultFromGO;
//                if(message.obj!=null){
//                    Log.d("查询返回的结果为：",message.obj.toString());
//                }
//                //MyServerSocket.handlerMyServerSocket.sendMessage(message);
//                main2ActivityMessagHandler.sendMessage(message);
                socket.close();
                Log.d("客户端发送查询完成","客户端发送查询完成");
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
            e.printStackTrace();
        }
    }
    //qurry 和write 的区别在于qurry需要加一个标签，让组主服务端可以判别
    public void qurry(boolean forQurry,String source){
        try{
            String qurrySource = String.valueOf(forQurry)+"-"+source;
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(qurrySource);
            Log.d("客户端查询","客户端查询成功");
            bufferedWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
