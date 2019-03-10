package com.example.ht.d2d_one.communication;

import android.content.Intent;
import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.icn.ResourceRequestPacket;
import com.example.ht.d2d_one.util.FileTransfer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientSocket extends Thread {
    private String label;
    private String host = null;
    private String content =null;
    final private int QURRYFROMGO =3;
    private int port;
    private int RRTTL = 8;
    private String RRMAC;
    private String pathInfo;
    private String tag;
    private String resourceName;
    private String typeOfResourcename;
    private FileTransfer fileTransfer;
    private ResourceRequestPacket resourceRequestPacket = new ResourceRequestPacket(RRTTL,RRMAC,pathInfo,resourceName,typeOfResourcename);

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
    public ClientSocket(String host,int port,String label,FileTransfer fileTransfer){
        this.host = host;
        this.port = port;
        this.label = label;
        this.fileTransfer = fileTransfer;
    }
    public ClientSocket(String host,int port,String label,String tag,String content){
        this.host = host;
        this.port = port;
        this.label = label;
        this.tag =tag;
        this.content = content;
    }
    public ClientSocket(String host, int port, String label, ResourceRequestPacket resourceRequestPacket){
        this.host = host;
        this.port = port;
        this.label = label;
        this.resourceRequestPacket = resourceRequestPacket;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    };

    public void run(){
        try{
            Socket socket = new Socket(host,port);
            if(label.equals("read")){
                //子线程中获取到数据，需要传给主线程。
                String getContent = read(socket);
                socket.close();
            }else if(label.equals("write")&&tag == null){
                //首先获取资源，字符串类型,然后调用write方法
                write(socket,content);
                socket.close();
                Log.d("客户端写完毕",content);
            }else if (label.equals("test")){
                write(socket,content);
                socket.close();
                Log.d("ceshi!!!!!",content);
            }else if(label.equals("query")){
                query(socket,true,content);
                socket.close();
                Log.d("客户端发送查询完成","客户端发送查询完成");
            }else if(tag.equals("interGroup")){
                //不关闭此Socket,并将该Socket保存的ISI表中,1表示LC组主就是本设备
                write(socket,content);
                Log.d("客户端写完毕","客户端写完了");
                BasicWifiDirectBehavior.icnOfGO.addInterGroupSocketInfo("1",socket);
                while(true){
                    String unicastMessage = read(socket);
                    if(unicastMessage!=null){
                        Log.d("来自网关节点的单播信息",unicastMessage);
                    }
                }
            }
//            else if(label.equals("transfer")){
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                FileTransfer fileTransfer = new FileTransfer(,socket);
//            }
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
    //qurry 和write 的区别在于qurry需要加一个标签，让组主服务端可以判别
    public void query(Socket socket,boolean forQurry,String source){
        try{
            String qurrySource = String.valueOf(forQurry)+"+"+source;
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(qurrySource);
            Log.d("客户端查询","客户端查询成功");
            bufferedWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void writeFile(Socket socket,FileTransfer fileTransfer){
        try{
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(fileTransfer);
            InputStream inputStream = null;
            if(!fileTransfer.getFilePath(fileTransfer.getHead()).equals("wrong")){
                inputStream = new FileInputStream(fileTransfer.getFilePath(fileTransfer.getHead()));
            }
            byte buf[] = new byte[1024];
            int flag;
            while((flag = inputStream.read(buf))!=-1){
                outputStream.write(buf);
            }
            Log.d("这里是发送文件","发送文件成功");
            outputStream.close();
            objectOutputStream.close();
            inputStream.close();
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
