package com.example.ht.d2d_one.interGroupCommunication;

import android.util.Log;

import com.example.ht.d2d_one.util.FileTransfer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private Socket oldSocket;
    private String label;
    private String content;
    private FileTransfer fileTransfer;
    private InputStream inputStream;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    public SocketReuse(Socket socket){
        this.socket = socket;
    }
    public SocketReuse(Socket socket,String label,String content){
        this.socket =socket;
        this.label = label;
        this.content = content;
    }
    public SocketReuse(ObjectOutputStream objectOutputStream, String label, FileTransfer fileTransfer){
        this.objectOutputStream = objectOutputStream;
        this.label = label;
        this.fileTransfer = fileTransfer;
    }
    public SocketReuse(Socket socket,ObjectOutputStream objectOutputStream, String label, FileTransfer fileTransfer){
        this.socket =socket;
        this.objectOutputStream = objectOutputStream;
        this.label = label;
        this.fileTransfer = fileTransfer;
    }
    public SocketReuse(Socket socket,InputStream inputStream,ObjectInputStream objectInputStream,String label,FileTransfer fileTransfer){
        this.socket = socket;
        this.inputStream = inputStream;
        this.objectInputStream = objectInputStream;
        this.label = label;
        this.fileTransfer = fileTransfer;
    }
    public SocketReuse(Socket socket,ObjectOutputStream objectOutputStream,Socket oldSocket,String label,FileTransfer fileTransfer){
        this.socket = socket;
        this.objectOutputStream = objectOutputStream;
        this.oldSocket = oldSocket;
        this.label = label;
        this.fileTransfer = fileTransfer;
    }
    public void run(){
        if(label.equals("write")){
            write(content);
        }else if(label.equals("read")){
            read();
        }else if(label.equals("writeFile")){
            writeFile(fileTransfer);
        }else if(label.equals("writeFileTransfer")){
            writeFIleTransfer(fileTransfer);
        }else if(label.equals("readANDWrite")){
            readANDWrite(oldSocket,fileTransfer);
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
    public void writeFIleTransfer(FileTransfer fileTransfer){
        try{
            objectOutputStream.writeObject(fileTransfer);
            objectOutputStream.flush();
            Log.d("网关传输资源名称","传输资源名称成功");
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void writeFile(FileTransfer fileTransfer){
        try{
            OutputStream outputStream = socket.getOutputStream();
            objectOutputStream.writeObject(fileTransfer);
            InputStream inputStream = null;
            if(!fileTransfer.getFilePath(fileTransfer.getHead()).equals("wrong")){
                inputStream = new FileInputStream(fileTransfer.getFilePath(fileTransfer.getHead()));
            }
            byte [] buf = new byte[1024*8*512];
            int len =0;
            long total =0;
            while((len = inputStream.read(buf))!=-1){
                outputStream.write(buf,0,len);
                total +=len;
                if(total == fileTransfer.getLength()){
                    break;
                }
            }
            objectOutputStream.flush();
            outputStream.flush();
            inputStream.close();
            Log.d("复用socket写文件成功",fileTransfer.getHead());
        }catch(IOException e){
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

    /**
     *组主节点通过复用socket和oldSocket来完成接收同时转发
     * @param fileTransfer
     */
    public void readANDWrite(Socket oldSocket,FileTransfer fileTransfer){
        try{
            OutputStream outputStream = socket.getOutputStream();
            objectOutputStream.writeObject(fileTransfer);
            objectOutputStream.flush();
            InputStream inputStream = oldSocket.getInputStream();
            long total =0;
            int len =0;
            byte [] buf = new byte[1024*8*512];
//            while(total<fileTransfer.getLength()){
//                len = inputStream.read(buf);
//                outputStream.write(buf,0,len);
//                total +=len;
//            }
            while((len=inputStream.read(buf))!=-1){
                outputStream.write(buf,0,len);
                total +=len;
                if(total == fileTransfer.getLength()){
                    break;
                }
            }
            outputStream.flush();
            objectOutputStream.reset();
            inputStream.close();
            oldSocket.close();
            Log.d("节点复用socket转发文件",String.valueOf(total));
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
