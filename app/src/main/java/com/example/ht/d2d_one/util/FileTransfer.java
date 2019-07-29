package com.example.ht.d2d_one.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

public class FileTransfer implements Serializable{
    /**
     * head 通过head 得到文件传输的一些信息
     * head 的格式为：
     *       RRNMAC+path+sourceNmae+pathOfResource+flag+ipOfRON
     *      RRNMAC+path+pathOfResource+flag+num(RON节点发往组主节点)
     *     或者是：RRNMAC+path+pathOFResource+flag+num+ipNextHop(组主节点发送给网关节点)
     *   其中，path的格式为：GOMAC-gwMAC,GOMAC-gwMAC,......
     *   (1)中的flag是beginDataBack
     *   (2)中的flag是dataBack
     */
    private String head;
    private long length;
    public FileTransfer(String head){
        this.head = head;
    }
    public FileTransfer(String head,long length){
        this.head = head;
        this.length = length;
    }
    public String getHead(){
        return head;
    }
    public long getLength(){
        return length;
    }
    public String getFilePath(String head){
        String filePath = null;
        if(head.split("\\+")[4].equals("beginDataBack")){
            filePath = head.split("\\+")[3];
            filePath = filePath.replace(",","").replaceAll(" ","").replace("[","");
        }else if(head.split("\\+")[3].equals("dataBack")){
            filePath = head.split("\\+")[2];
            filePath = filePath.replace(",","").replaceAll(" ","").replace("[","");
        }else{
            return "wrong";
        }
        return filePath;
    }
}
