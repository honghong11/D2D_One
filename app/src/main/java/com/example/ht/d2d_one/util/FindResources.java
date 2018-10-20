package com.example.ht.d2d_one.util;

import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

import com.example.ht.d2d_one.DeviceListFragment;
import com.example.ht.d2d_one.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindResources{
    private List<String> resourceMovies = new ArrayList<>();
    private List<String> resourceMusics = new ArrayList<>();
    private List<String> resourcePackges = new ArrayList<>();
    private List<String> resourceWords = new ArrayList<>();
    private  String source = "";
    public FindResources(){}

    public void findResources(File dir) {
        File[] files;
        files = dir.listFiles();
        if (files == null) {
            source = "电影-音乐-安装包-文字";
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                findResources(file);
            } else {
                if (file.getName().endsWith(".mp4") || file.getName().endsWith(".mkv")) {
                    resourceMovies.add(file.getAbsolutePath()+"+"+file.getName()+"|");
                    //resource.put("movie",resourceMovies);
                }
                if (file.getName().endsWith(".mp3")) {
                    resourceMusics.add(file.getAbsolutePath()+"+"+file.getName()+"|");
                    //resource.put("music",resourceMusics);
                }
                if (file.getName().endsWith(".apk")) {
                    resourcePackges.add(file.getAbsolutePath()+"+"+file.getName()+"|");
                    //resource.put("package",resourcePackges);
                }
                if (file.getName().endsWith(".pdf")) {
                    resourceWords.add(file.getAbsolutePath()+"+"+file.getName()+"|");
                    //resource.put("word",resourceWords);
                }
            }
        }
        source = resourceMovies.toString() + "-" +resourceMusics.toString() + "-"
                + resourcePackges.toString() + "-" + resourceWords.toString();
    }
    public String getResources(){
        String resources = " ";
        File dir = Environment.getExternalStorageDirectory();

        findResources(dir);
        resources = source;
        Log.d("resource 是什么",resources);
        return resources;
    }
}
