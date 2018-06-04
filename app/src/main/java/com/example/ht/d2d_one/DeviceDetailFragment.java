package com.example.ht.d2d_one;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DeviceDetailFragment extends Fragment{
    private View mContentView = null;
    public void onActivityCreated(Bundle saveInstanceState){
        super.onActivityCreated(saveInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup containter, Bundle saveInstanceState){
        mContentView = inflater.inflate(R.layout.device_detail,null);
        return mContentView;
    }
}
