package com.example.ht.d2d_one.interGroupCommunication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * socket长连接，就是复用socket。在需要的地方新建此类并调用对应的函数。
 * 所要复用的socket
 * write所需要的content
 * read函数返回一个content
 */
public class SocketReuse extends Thread{
    private Socket socket;
    private String label;
    private String content;
    public SocketReuse(Socket socket){
        this.socket = socket;
    }
    public SocketReuse(Socket socket,String label,String content){
        this.socket =socket;
        this.label = label;
        this.content = content;
    }
    public SocketReuse(Socket socket, String label){
        this.socket =socket;
        this.label = label;
    }
    public void run(){
        if(label.equals("write")){
            write(content);
        }else if(label.equals("read")){
            read();
        }
    }
    public void write(String content){
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(content);
            //写入换行符，完成一次数据传输
            bufferedWriter.write('\n');
            bufferedWriter.write("\n");
            bufferedWriter.flush();
            Log.d("复用socket写成功",content);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public String read(){
        String message = "";
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            message = bufferedReader.readLine();
        }catch (IOException e){
            e.printStackTrace();
        }
        return message;
    }
//    public String read(){
//        String content ="";
//        try{
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            content = bufferedReader.readLine();
//            if(content!=null){
//                Log.d("复用Socekt读的信息",content);
//            }
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//        return content;
//    }
//    public void write(String content){
//        try{
//            OutputStream os = socket.getOutputStream();
//            OutputStreamWriter ow = new OutputStreamWriter(os);
//            BufferedWriter writer = new BufferedWriter(ow);
//            writer.write(content);
//            writer.flush();
//            //%%%%作为内容结束的标志。
//            //bufferedWriter.write(content+"-"+"%%%%");
//            Log.d("复用Socket写完成","复用写完成啦啦啦");
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }
}
