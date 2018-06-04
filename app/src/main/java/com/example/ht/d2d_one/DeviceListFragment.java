package com.example.ht.d2d_one;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DeviceListFragment extends ListFragment {
      View mContentView = null;
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        mContentView = inflater.inflate(R.layout.device_list,null);
        return mContentView;
    }
}
