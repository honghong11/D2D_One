package com.example.ht.d2d_one;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String TRG="D2D_One";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private boolean isWifiP2pEnabel = false;
    private BroadcastReceiver receiver =null;
    private final IntentFilter intentfilter = new IntentFilter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //这个很关键
        intentfilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    public void onResume(){
        super.onResume();
        receiver = new WifiDirectBroadcastReceiver(manager,channel,this);
        registerReceiver(receiver,intentfilter);
    }

    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onStop(){
        super.onStop();
    }
    public void onDestroy(){
        super.onDestroy();
    }
    public void setIsWifiP2pEnable(boolean isWifiP2pEnable){
        this.isWifiP2pEnabel = isWifiP2pEnable;
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.atn_direct_enable:
                if(manager!=null&&channel!=null){
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
                else{
                    Log.e(TRG,"channel or manager is null");
                }
                return true;
            case R.id.atn_direct_discovery:
                if(!isWifiP2pEnabel){
                     Toast.makeText(MainActivity.this,"enable p2p from action bar above or system settings",
                             Toast.LENGTH_SHORT).show();
                }
                else{
                    manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this,"Discovery Initiated",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(MainActivity.this,"Discovery Failed"+ reason ,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
