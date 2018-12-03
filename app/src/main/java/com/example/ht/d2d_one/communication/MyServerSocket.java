package com.example.ht.d2d_one.communication;

import android.os.Message;
import android.util.Log;

import com.example.ht.d2d_one.icn.IcnOfGO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import static com.example.ht.d2d_one.intraGroupCommunication.IntraCommunication.main2ActivityMessagHandler;
import static com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior.messageHandler;

public class MyServerSocket extends Thread{
    private ServerSocket serverSocket;
    private String label;
    private int port;
    private List<Socket> socketList = new ArrayList<Socket>();
    private String MAC = null;
    private String tag;
    private IcnOfGO icnOfGO = new IcnOfGO();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    private Socket s;
    private static List<String> source;

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

    public MyServerSocket(String MAC, int port, String label, String tag){
        this.MAC = MAC;
        this.port = port;
        this.label = label;
        this.tag = tag;
    }
    //因为许多Icn操作需要在子线程中做，需要IcnOfGO去操作其中的各个表格
    public MyServerSocket(IcnOfGO icnOfGO,String MAC,int port,String label){
        this.icnOfGO = icnOfGO;
        this.MAC = MAC;
        this.port = port;
        this.label = label;
    }
    public MyServerSocket(String MAC, int port, String label){
        this.MAC = MAC;
        this.label = label;
        this.port = port;
    }
    public void run(){
        try{
        serverSocket = new ServerSocket(port);
        if(tag!=null){
            if(tag.equals("clent")){
                while(true){
                    Log.d("MyServer","ClientMyServer is listening...");
                    s = serverSocket.accept();
                    if(label.equals("read")){
                        String resultFromGO = read(s);
                        s.close();
                        Message message = Message.obtain();
                        message.what = 3;
                        message.obj = resultFromGO;
                        if(message.obj!=null){
                            Log.d("从组主节点获取的资源查询结果：",message.obj.toString());
                        }
                        main2ActivityMessagHandler.sendMessage(message);
                    }
                }
            }else if(tag.equals("GW")){
                while (true){
                    Log.d("GMServer","GMServer is listening...");
                    s = serverSocket.accept();
                    if(label.equals("read")){
                        String aimGO = read(s);
                        s.close();
                        Message message = Message.obtain();
                        message.what = 3;
                        message.obj = aimGO;
                        if(message.obj!=null){
                            Log.d("推荐组主的MAC地址",message.obj.toString());
                        }
                        messageHandler.sendMessage(message);
                    }
                }
            }
        }else{
            while(true){
                Log.d("MyServer","GOMyServer is listening...");
                s = serverSocket.accept();
                socketList.add(s);
                /**
                 * accept之后转入到一个子线程socket中，负责与客户端socket之间到读写交互
                 * 对于组主所开的线程，再加一个icnOfGO字段
                 */
                new Thread(new MyServerSocketThread(icnOfGO,MAC,s,label,socketList.size())).start();
            }
        }
        }catch(IOException e){
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
        }
        return content;
    }
    public void close(){
        try{
            if(s!=null){
                s.close();
            }
            serverSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
