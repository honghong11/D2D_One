package com.example.ht.d2d_one;

import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DeviceListFragment.DeviceActionListener,WifiP2pManager.DnsSdServiceResponseListener,WifiP2pManager.DnsSdTxtRecordListener{
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String instanceName = "FirstService";
    public static final String serviceType = "_handover._tcp";
    public static final String TRG="D2D_One";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private boolean isWifiP2pEnabel = false;
    private BroadcastReceiver receiver =null;
    private WifiP2pDevice wifiP2pDevice ;
    private WifiP2pDeviceList wifiP2pDeviceList;
    private wifiDeviceWithLabel extendDevice;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private DeviceDetailFragment deviceDetailFragment;
    Context context;
    private final IntentFilter intentfilter = new IntentFilter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //除开过滤广播的的IntentFilter可以在代码中创建外，其它的IntentFilter都得在AndroidManifest.xml中给设置。
        //在广播接收器中的意图需要在这里加到意图过滤器中，否则广播接收器不会接收该意图
        intentfilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentfilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentfilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentfilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
//        DeviceListFragment deviceListFragment = new DeviceListFragment();
//        getFragmentManager().beginTransaction().add(R.id.linearLayout1,deviceListFragment,"services").commit();
        deviceDetailFragment = new DeviceDetailFragment();
        getFragmentManager().beginTransaction().add(R.id.linearLayout1,deviceDetailFragment,"services").commit();
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
                    serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
                    manager.addServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this,"add service succeed",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(MainActivity.this,"add service failed",Toast.LENGTH_SHORT).show();
                        }
                    });
                    //必须要调用addServiceRequest函数，而WifiP2pDnsSdServiceRequest.newInstance()中一定是service request for Bonjour，就必须要回调setDnsSdResponseListeners
//                    manager.setServiceResponseListener(channel, new WifiP2pManager.ServiceResponseListener() {
//                        @Override
//                        public void onServiceAvailable(int protocolType, byte[] responseData, WifiP2pDevice srcDevice) {
//                            Log.d(MainActivity.TRG,srcDevice.deviceName+"~~~~~~~~~~~~~~~~~~~~~~~");
//                        }
//                    });
                    manager.setDnsSdResponseListeners(channel, new WifiP2pManager.DnsSdServiceResponseListener() {
                        @Override
                        public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
//manager.findFragmentById(); //根据ID来找到对应的Fragment实例，主要用在静态添加fragment的布局中，因为静态添加的fragment才会有ID
//manager.findFragmentByTag();//根据TAG找到对应的Fragment实例，主要用于在动态添加的fragment中，根据TAG来找到fragment实例
//manager.getFragments();//获取所有被ADD进Activity中的Fragment
//  DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.layout.device_list);
                            DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager().findFragmentByTag("services");
                            DeviceDetailFragment.WifiServiceAdapter adapter = (DeviceDetailFragment.WifiServiceAdapter)fragment.getListAdapter();
                            wifiDeviceWithLabel service = new wifiDeviceWithLabel();
                            service.device = srcDevice;
                            service.label = instanceName;
                            adapter.add(service);
                            adapter.notifyDataSetChanged();
                            //((DeviceDetailFragment.WifiServiceAdapter)fragment.getListAdapter()).notifyDataSetChanged();
                            Log.d(MainActivity.TRG, "onBonjourServiceAvailable "
                                    + instanceName);
                            //应该在获取数据的地方调用显示的页面。可以设置一个adapter来处理页面
                        }
                    }, new WifiP2pManager.DnsSdTxtRecordListener() {
                        @Override
                        public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {

                        }
                    });
                    manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this,"discoveryService Initiated",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(MainActivity.this,"discoveryService Failed"+ reason ,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice){
        Log.d("DnsSdServiceAvailable",instanceName+" "+registrationType+" "+srcDevice.deviceName);
    }
    @Override
    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice){

    }
//    public void updateThisDevice(WifiP2pDevice wifiP2pDevice){
//             this.wifiP2pDevice = wifiP2pDevice;
//             Log.d("更新本设备信息：：：：：",wifiP2pDevice.toString());
//    }

//    public  static String getWifiMac(Context ctx) {
//        WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
//        WifiInfo info = wifi.getConnectionInfo();
//        String str = info.getMacAddress();
//        if (str == null) str = "";
//        Log.d("0000000000000000",str);
//        return str;
//    }
    @Override
    public void connect(final WifiP2pConfig wifiP2pConfig){
        manager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this,"connect failed, retry.原因为："+reason,Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void disconnect(){
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(MainActivity.TRG,"disconnect is okkkkkkkkkkk");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(MainActivity.TRG,"disconnect failed:"+ reason);
            }
        });
    }
    @Override
    public void publishService(String string){
        //现在理解的instanceName就是所发布的服务,那么问题就是如何将instanceName的内容显示到组标签中。
        String dyInstanceName = string;
        Map<String,String> record = new HashMap<String,String>();
        record.put(TXTRECORD_PROP_AVAILABLE,"visable");
//        manager.setDnsSdResponseListeners(channel,this,this);
//        WifiP2pDnsSdServiceRequest wifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance(instanceName,serviceType);
//        manager.addServiceRequest(channel, wifiP2pDnsSdServiceRequest, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                Log.d(MainActivity.TRG,"添加service discovery request successfullllllllllllllllllll");
//            }
//
//            @Override
//            public void onFailure(int reason) {
//                Log.d(MainActivity.TRG,"添加service discovery request failed");
//            }
//        });

        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(dyInstanceName,serviceType,record);
        manager.addLocalService(channel, wifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(MainActivity.TRG,"addlocalService successfullllllllllllllllllll");
            }

            @Override
            public void onFailure(int reason) {
                    Log.d(MainActivity.TRG,"Failed addlocalServiceeeeeeeeeeeeeeeeeee");
            }
        });
    }

    @Override
    public void createGroup() {
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "createGroup successfully",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "failed to createGroup",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
