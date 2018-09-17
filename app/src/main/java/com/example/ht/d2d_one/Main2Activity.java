package com.example.ht.d2d_one;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends ListActivity {
    private ArrayList<WifiP2pDevice> groupClient = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Button button = findViewById(R.id.quitCluster);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("看这里","dddddddddddddddddddddddddis");
                finish();
            }
        });
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Log.d("clientList:::::::::::",groupClient.toString());
        this.setListAdapter(new WifiClientListAdapter(this,R.layout.clents_list,groupClient));
    }
    protected void onResume(){
        super.onResume();
    }

    protected void onPause(){
        super.onPause();
    }

    protected void onStop(){
        super.onStop();
    }
    protected void onDestroy(){
        super.onDestroy();
    }
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        processExtraData();
        Log.d("device_______________","1111111111111111111111");
    }

    private void processExtraData(){
        Intent intent = getIntent();
        String string = intent.getStringExtra("device");
        Log.d("device_______________",string);
    }

    public class WifiClientListAdapter extends ArrayAdapter<WifiP2pDevice> {
        private List<WifiP2pDevice> options;
        public WifiClientListAdapter(Context context, int resource, List<WifiP2pDevice> items){
            super(context,resource,items);
            options = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            //获取LayoutInflater实例的三种方式之一，但这三种方式在根本上都是调用getSystemService(Context.Layout_inflater_service)
            if(v==null){
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.clents_list,null);
            }
            return v;
        }

        @Override
        public void add(WifiP2pDevice wifiP2pDevice){
        }

        @Override
        public void remove(WifiP2pDevice wifiP2pDevice) {
        }
    }
}
