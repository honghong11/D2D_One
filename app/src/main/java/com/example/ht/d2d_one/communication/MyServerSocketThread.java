package com.example.ht.d2d_one.communication;

import android.os.Message;
import android.util.Log;

import com.example.ht.d2d_one.DeviceListFragment;
import com.example.ht.d2d_one.util.MatchingAlgorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.example.ht.d2d_one.DeviceListFragment.messageHandler;

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
            String clientIpAddrss = socket.getInetAddress().toString();
            clientIpAddrss = clientIpAddrss.substring(1);
            Log.d("此连接的组员设备分配的IP地址为：", ":/-"+clientIpAddrss);
            if(label.equals("read")){
                String getResource = read(socket);
                String[]messageFromClient = getResource.split("-");
                //如果第一个部分为字符串true，则表明组主收到的为查询
                if(messageFromClient[0].equals("true")){
                    Map<String,String> resultMap = new HashMap<>();
                    if(messageFromClient[1]!=null&&messageFromClient[2]!=null){
                        //资源匹配
                        String qurryName = messageFromClient[1];
                        String qurryType = messageFromClient[2];
                        Log.d("类别标签：::::",qurryType);
                        Log.d("类别标签：::::",qurryName);
                        String dataToBack;
                        switch (qurryType){
                            case "movie":
                                MatchingAlgorithm matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryMovieMap());
                                resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryMovieMap());
                                if(resultMap!=null){
                                    dataToBack = mapToString(resultMap);
                                    if(dataToBack!=null){
                                        Log.d("组主节点处的结果信息",dataToBack);
                                        ClientSocket clientSocketGO  = new ClientSocket(clientIpAddrss,30001,"write",dataToBack);
                                        clientSocketGO.start();
                                    }
                                }else{
                                    dataToBack = "没有查询到信息";
                                }
                                break;
                            case "music":
                                matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryMusicMap());
                                resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryMusicMap());
                                dataToBack = mapToString(resultMap);
                                write(dataToBack);
                                socket.close();
                                break;
                            case "packet":
                                matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryPackageMap());
                                resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryPackageMap());
                                dataToBack = mapToString(resultMap);
                                write(dataToBack);
                                socket.close();
                                break;
                            case "word":
                                matchingAlgorithm = new MatchingAlgorithm(qurryName,messageHandler.getQurryWordMap());
                                resultMap = matchingAlgorithm.matchingCharacterAlgorithm(qurryName,messageHandler.getQurryWordMap());
                                dataToBack = mapToString(resultMap);
                                write(dataToBack);
                                socket.close();
                                break;
                            default:
                                Log.d("出现错误","未找到匹配的资源类型");
                        }
                    }
                }else{
                    Log.d("资源清单:::::::",getResource);
                    //将资源从该子线程发送到主线程中，跨越一层线程
                    Message message = Message.obtain();
                    message.what = MRESOURCE;
                    message.obj = getResource;
                    if(message.obj!=null){
                        Log.d("寻找资源的子线程中message的信息：",message.obj.toString());
                    }
                    //MyServerSocket.handlerMyServerSocket.sendMessage(message);
                    messageHandler.sendMessage(message);
                    socket.close();
                }
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

    /**
     *
     * @param map map类型不能直接在线程之间进行传输
     * @return String类型便于线程之间的数据传输, 返回值的格式为：mac+path-name*mac+path-name......*
     */
    public String mapToString(Map<String,String> map){
        Set<String> indexs = map.keySet();
        String[] firstPartOfMap = new String[indexs.size()];
        String[] secondPartOfMap = new String[indexs.size()];
        String resultString = null;
        int i =0;
        for(String index:indexs){
            firstPartOfMap[i] = index;
            secondPartOfMap[i] = map.get(index);
        }
        for(i =0;i<indexs.size();i++){
            resultString = firstPartOfMap[i]+"-"+secondPartOfMap[i]+"\\*";
        }
        return resultString;
    }
}
