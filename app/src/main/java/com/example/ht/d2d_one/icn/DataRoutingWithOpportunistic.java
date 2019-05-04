package com.example.ht.d2d_one.icn;

import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;
import com.example.ht.d2d_one.interGroupCommunication.SocketReuse;
import com.example.ht.d2d_one.util.FileTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class DataRoutingWithOpportunistic extends Thread{
    private String host;
    private int port;
    private String label;
    private Socket oldSocket;
    private FileTransfer fileTransfer;
    private String path;
    private Socket newSocket;
    private ObjectOutputStream objectOutputStream;
    public DataRoutingWithOpportunistic(String host,int port,String label,Socket oldSocket,FileTransfer fileTransfer,String path){
        this.host = host;
        this.port = port;
        this.label = label;
        this.oldSocket = oldSocket;
        this.fileTransfer = fileTransfer;
        this.path = path;
    }
    public DataRoutingWithOpportunistic(String label,Socket oldSocket,Socket newSocket,FileTransfer fileTransfer,ObjectOutputStream objectOutputStream,String path){
        this.label = label;
        this.oldSocket = oldSocket;
        this.newSocket = newSocket;
        this.fileTransfer = fileTransfer;
        this.objectOutputStream = objectOutputStream;
        this.path = path;
    }
    public void run() {
        try{
            if(label.equals("gwToP2p")){
                Socket socket = new Socket(host,port);
                //网关节点转发给非LC组主节点，先将文件存储到文件中，再转发
                cacheFile(path,oldSocket,fileTransfer,"lc");
                forwardFileNoLC(path,socket,fileTransfer);
            }else if(label.equals("gwToWiFi")){
                cacheFile(path,oldSocket,fileTransfer,"nolc");
                forwardFileToWiFi(path,newSocket,objectOutputStream,fileTransfer);
            }else if(label.equals("wifiGoToGw")){
                Socket socket = new Socket(host,port);
                cacheFile(path,oldSocket,fileTransfer,"nolc");
                forwardFileNoLC(path,socket,fileTransfer);
            }else if(label.equals("p2pGoToGw")){
                Socket socket = new Socket(host,port);
                cacheFile(path,oldSocket,fileTransfer,"lc");
                forwardFileNoLC(path,socket,fileTransfer);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param path 路径信息
     * @param socket 上一跳的socket
     * @param fileTransfer 文件信息
     */
    private void cacheFile(String path,Socket socket,FileTransfer fileTransfer,String tag){
        try{
            path = path.replace(",","").replace(" ","").replace("[","");
            String []pathInfo = path.split("/");
            String newPath = "/";
            for(int i =0;i<pathInfo.length-1;i++){
                newPath = newPath+"/"+pathInfo[i];
            }
            File file = new File(newPath);
            if(!file.exists()){
                file.mkdirs();
            }
            file = new File(path);
            OutputStream outputStream = new FileOutputStream(file);
            byte [] buf = new byte[1024*8*512];
            long total =0;
            int len = 0;
            while((len=socket.getInputStream().read(buf))!=-1){
                outputStream.write(buf,0,len);
                total += len;
                if(total>=fileTransfer.getLength()){
                    break;
                }
            }
            Log.d("cache文件的大小",String.valueOf(total));
            outputStream.flush();
            outputStream.close();
            if(tag.equals("nolc")){
                socket.getInputStream().close();
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void forwardFileNoLC(String path, Socket socket,FileTransfer fileTransfer){
        path = path.replace(" ","").replace(",","").replace("[","");
        try{
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(fileTransfer);
            objectOutputStream.flush();
            InputStream inputStream = new FileInputStream(path);
            byte buf[] = new byte[1024*8*512];
            long total = 0;
            int len = 0;
            //TODO 显示文件传输的进度
            while((len = inputStream.read(buf))!=-1){
                outputStream.write(buf,0,len);
                total += len;
                if(total == fileTransfer.getLength()){
                    break;
                }
            }
            Log.d("经cache后转发的文件的大小",String.valueOf(total));
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            objectOutputStream.close();
            socket.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void forwardFileToWiFi(String path,Socket socket,ObjectOutputStream objectOutputStream,FileTransfer fileTransfer){
        path = path.replace(" ","").replace(",","").replace("[","");
        try{
            OutputStream outputStream = socket.getOutputStream();
            objectOutputStream.writeObject(fileTransfer);
            objectOutputStream.flush();
            InputStream inputStream = new FileInputStream(path);
            byte buf[] = new byte[1024*8*512];
            long total = 0;
            int len = 0;
            //TODO 显示文件传输的进度
            while((len = inputStream.read(buf))!=-1){
                outputStream.write(buf,0,len);
                total += len;
                if(total == fileTransfer.getLength()){
                    break;
                }
            }
            outputStream.flush();
            objectOutputStream.reset();
            inputStream.close();
            Log.d("cache下节点复用socket转发文件大小",String.valueOf(total));
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
