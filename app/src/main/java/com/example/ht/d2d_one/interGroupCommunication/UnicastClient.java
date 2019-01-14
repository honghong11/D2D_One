package com.example.ht.d2d_one.interGroupCommunication;


import android.util.Log;

import com.example.ht.d2d_one.bisicWifiDirect.BasicWifiDirectBehavior;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * 实现Socket的复用，可读socket中的内容，也可向socket中写数据
 */
public class UnicastClient extends Thread{
    private boolean firstInfoFromGW = true;
    private String host;
    private int port;
    private String label;
    private String content;
    private String macLC;
    public UnicastClient(String host,int port,String label,String content) {
        this.host = host;
        this.port = port;
        this.label = label;
        this.content = content;
    }

    /**
     * 组间单播客户端，第一次时候，LC组主向网关节点发起TCP请求，并保存该Socket到本LC组主节点。其中，第一个字段是网关节点的mac地址
     */
    @Override
    public void run(){
        try{
            Socket socket = new Socket(host,port);
             if(label.equals("write")){
                write(socket,content);
                socket.setKeepAlive(true);

                //SocketReuse socketReuse = new SocketReuse(socket);
                 String message = "";
                while (true){
                    //由于bufferedReader.readLineshiyige阻塞是方法，所以本方法也是阻塞式的
                    if(firstInfoFromGW){
                        //第一次接收到网关的信息是网关节点的MAC地址
                        //String message = socketReuse.read();
                        try{
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            message = bufferedReader.readLine();
                        }catch (IOException e){
                            e.printStackTrace();
                        }

                        Log.d("来自网关节点的单播消息",message);
                        String [] messages = message.split("\\+");
                        GateWay gateWay = new GateWay(true,messages[1]);
                        BasicWifiDirectBehavior.icnOfGO.addInterGroupSocketInfo("1",socket);
                        //添加网关节点到LC组主的GWT表中
                        BasicWifiDirectBehavior.icnOfGO.addGMTable(gateWay,macLC);

                        while (true){
                            try{
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                message = bufferedReader.readLine();
                                Log.d("来自网关节点的单播消息2",message);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }else{
                        while (true){
                            try{
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                message = bufferedReader.readLine();
                                Log.d("来自网关节点的单播消息2",message);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //对于read或者write操作，不关闭于socket相关的流
    private String read(Socket socket){
        String message = "";
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            message = bufferedReader.readLine();
        }catch (IOException e){
            e.printStackTrace();
        }
        return  message;
    }
    public void write(Socket socket, String content){
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(content);
            //bufferWriter.readline()是一个阻塞方法，如果没有读到换行符，或者结束符，不会结束readline,我又不能close，所以在发送内容字段后，发送换行符。
            bufferedWriter.write('\n');
            bufferedWriter.write("\n");

//            bufferedWriter.close();
            bufferedWriter.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
