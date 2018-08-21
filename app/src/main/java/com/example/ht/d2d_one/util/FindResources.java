package com.example.ht.d2d_one.util;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindResources {
    private List<String> resourceMovies = new ArrayList<>();
    private List<String> resourceMusics = new ArrayList<>();
    private List<String> resourcePackges = new ArrayList<>();
    private List<String> resourceWords = new ArrayList<>();
    public FindResources(){}
    public void findResources(File dir,Map<String,List<String>> resource) {
        File[] files;
        files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                findResources(file, resource);
            } else {
                if (file.getName().endsWith(".mp4") || file.getName().endsWith(".mkv")) {
                    resourceMovies.add(file.getAbsolutePath().toString()+"---"+file.getName()+"///");
                    resource.put("movie",resourceMovies);
                }
                if (file.getName().endsWith(".mp3")) {
                    resourceMusics.add(file.getAbsolutePath().toString()+"---"+file.getName()+"///");
                    resource.put("music",resourceMusics);
                }
                if (file.getName().endsWith(".apk")) {
                    resourcePackges.add(file.getAbsolutePath().toString()+"---"+file.getName()+"///");
                    resource.put("package",resourcePackges);
                }
                if (file.getName().endsWith(".pdf")) {
                    resourceWords.add(file.getAbsolutePath().toString()+"---"+file.getName()+"///");
                    resource.put("word",resourceWords);
                }
            }
        }
    }
    public Map getResources(){
        Map<String,List<String>> resources = new HashMap<>();
        File dir = Environment.getExternalStorageDirectory();
        findResources(dir,resources);
        return resources;
    }
}
