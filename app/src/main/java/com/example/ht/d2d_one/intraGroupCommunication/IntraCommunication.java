package com.example.ht.d2d_one.intraGroupCommunication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ht.d2d_one.R;
import com.example.ht.d2d_one.communication.ClientSocket;
import com.example.ht.d2d_one.communication.MyServerSocket;

import java.util.ArrayList;
import java.util.List;

public class IntraCommunication extends Activity {
    //此处应该是资源查询结果，包括资源名称+对应的设备地址
    private ArrayList<String> sourceResultList = new ArrayList<>();
    private boolean isGO = false;
    private String deviceAddress;
    private String mDeviceIpAddress;
    private String resultQurryFromGO;
    public boolean isClickQurrySourceButton = false;
    public static Main2ActivityMessagHandler main2ActivityMessagHandler = new Main2ActivityMessagHandler();
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
        Button buttonQurry = findViewById(R.id.findSource);
        Button buttonQurryResult = findViewById(R.id.showResult);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            isGO = bundle.getBoolean("isGO");
            deviceAddress = bundle.getString("deviceAddress");
        }
        buttonQurry.setOnClickListener(new View.OnClickListener(){
            EditText editTextSourceQurried = findViewById(R.id.resourceNameQurried);
            Spinner spinnerSourceType = findViewById(R.id.typeOfSource);
            @Override
            public void onClick(View v){
                //如果输入信息合法，则将查询信息发送给组主，并开启组员设备的socket服务监听模式
                //还需要判断本设备是否为组主！！！！
                if(editTextSourceQurried!=null&&spinnerSourceType!=null&&!spinnerSourceType.toString().equals("资源类别")){
                    String qurryMessage = editTextSourceQurried.getText().toString()+"-"+spinnerSourceType.getSelectedItem().toString();
                    if(!isGO){
                        isClickQurrySourceButton = true;
                        //如果不是组主，则开启客户socket写，发送查询信息
                        new ClientSocket("192.168.49.1",30000,"qurry",qurryMessage).start();
                        //开启组员设备的服务端监听
                        //！！！！这个deviceAddress应该不是Ip，这里先写在这里
                        Log.d("开启服务端的mac地址是：",deviceAddress);
                        MyServerSocket myServerSocketTwo = new MyServerSocket(deviceAddress,30001,"read", "client");
                        myServerSocketTwo.start();
                        Toast.makeText(IntraCommunication.this, "资源查询成功",Toast.LENGTH_SHORT).show();
                    }else{
                        //如果是组主，本地查询
                        isClickQurrySourceButton = true;
                    }
                }
            }
        });
        /**
         * 查询结果展示按钮，用来显示查询结果，也可以用来刷新
         */
        buttonQurryResult.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(isClickQurrySourceButton){
                    if(main2ActivityMessagHandler.resultQueryFromGO!=null){
                        sourceResultList = (ArrayList<String>) StringToList(main2ActivityMessagHandler.resultQueryFromGO);
                        Log.d("点击查看查询结果为：：：：：", sourceResultList.toString());
                        ListView listView = (ListView) findViewById(R.id.list_Main2Activity);
                        ResultSourceAdapter resultSourceAdapter = new ResultSourceAdapter(IntraCommunication.this,R.layout.service_list,sourceResultList);
                        listView.setAdapter(resultSourceAdapter);
                    }else{
                        Log.d("查询结果为空值","hh"+main2ActivityMessagHandler.resultQueryFromGO);
                        Toast.makeText(IntraCommunication.this,"查询结果为空值",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            /**
             *
             * @param stringResult 具体格式为：MAC-NAME*MAC-NAME...
             * @return 其具体的格式为：MAC-NAME,MAC-NAME,
             */
            public List<String> StringToList(String stringResult){
                List<String> resultSource = new ArrayList<>();
                String[] singleResult = stringResult.split("\\*");
                int i=0;
                for(;i<=singleResult.length-1;i++){
                    resultSource.add(singleResult[i]);
                }
                Log.d("resultSource",resultSource.toString());
                return resultSource;
            }
        });
        //this.setListAdapter(new ResultSourceAdapter(this,R.layout.service_list,sourceResultList));
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
    public static class Main2ActivityMessagHandler extends Handler{
        private String resultQueryFromGO;
        public String getResultQurryFromGO() {
            return resultQueryFromGO;
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==3){
                resultQueryFromGO = (String)msg.obj;
                Log.d("查询返回信息",resultQueryFromGO);
            }
        }
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

    public class ResultSourceAdapter extends ArrayAdapter<String> {
        private List<String> options;
        private String [] resultSource;
        public ResultSourceAdapter(Context context, int resource, List<String> items){
            super(context,resource,items);
            options = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            //获取LayoutInflater实例的三种方式之一，但这三种方式在根本上都是调用getSystemService(Context.Layout_inflater_service)
            if(v==null){
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.service_list,null);
            }
            Log.d("options是否为空",options.toString());
                String singleResult = options.get(position);
                if(singleResult!=null){
                    TextView result = (TextView) v.findViewById(R.id.ron_MAC_Name);
                    if(result!=null){
                        Log.d("单个结果为",singleResult.toString());
                        result.setText(singleResult.toString());
                    }
                }
            return v;
        }

        @Override
        public void add(String s){
        }

        @Override
        public void remove(String s) {
        }
    }
}
