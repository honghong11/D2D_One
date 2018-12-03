package com.example.ht.d2d_one.bisicWifiDirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.example.ht.d2d_one.R;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity mainActivity;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity mainActivity){
        super();
        this.manager = manager;
        this.channel = channel;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                mainActivity.setIsWifiP2pEnable(true);
            }else{
                mainActivity.setIsWifiP2pEnable(false);
            }
            Log.d(MainActivity.TRG,"wifiP2p state changed - "+state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(manager!=null){
                //request the current peer list
                manager.requestPeers(channel,(WifiP2pManager.PeerListListener)
                        mainActivity.getFragmentManager().findFragmentById(R.id.list_frag));
            }
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            if(manager==null){
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()){
                manager.requestConnectionInfo(channel,(WifiP2pManager.ConnectionInfoListener)mainActivity
                        .getFragmentManager().findFragmentById(R.id.list_frag));
                manager.requestGroupInfo(channel,(WifiP2pManager.GroupInfoListener)mainActivity
                        .getFragmentManager().findFragmentById(R.id.list_frag));
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            Log.d(MainActivity.TRG,"触发WIFI_P2P_THIS_DEVICE_CHANGED_ACTION啦啦啦啦啦啦");
            BasicWifiDirectBehavior fragment = (BasicWifiDirectBehavior) mainActivity.getFragmentManager().findFragmentById(R.id.list_frag);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            Log.d(MainActivity.TRG,"触发WIFI_P2P_THIS_DEVICE_CHANGED_ACTION啦啦啦啦啦啦后的设备信息"+
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }
}
