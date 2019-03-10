package com.example.ht.d2d_one.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

public class FileTransfer implements Serializable{
    /**
     * head 通过head 得到文件传输的一些信息
     * head 的格式为：RRNMAC+path+name+pathOfResource+flag+ipRON(1)
     *     或者是：RRNMAC+path+name+flag+num+ipNextHop(2)
     *   其中，path的格式为：GOMAC-gwMAC,GOMAC-gwMAC,......
     *   (1)中的flag是beginDataBack
     *   (2)中的flag是dataBack
     */
    private String head;
    public FileTransfer(String head){
        this.head = head;
    }
    public String getHead(){
        return head;
    }
    public String getFilePath(String head){
        String filePath = null;
        if(head.split("\\+")[4].equals("beginDataBack")){
            filePath = head.split("\\+")[3];
        }else{
            return "wrong";
        }
        return filePath;
    }
}
