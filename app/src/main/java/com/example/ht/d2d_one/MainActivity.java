package com.example.ht.d2d_one;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DeviceListFragment.DeviceActionListener,
        WifiP2pManager.DnsSdServiceResponseListener,WifiP2pManager.DnsSdTxtRecordListener{
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String instanceName = "FirstService";
    public static final String serviceType = "_handover._tcp";
    public static final String TRG="D2D_One";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private boolean isWifiP2pEnabel = false;
    private BroadcastReceiver receiver =null;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private DeviceListFragment  deviceListFragment;
    private int count =0;

//localService 的添加和移除

    private String getInstanceName ="";

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
        //为deviceListFragment 添加tag
        deviceListFragment = new DeviceListFragment();
        getFragmentManager().beginTransaction().add(R.id.linearLayout1,deviceListFragment,"services").commit();
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
    //菜单栏： 包含开启WiFi和发现服务
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
//                    discoverPeers用于发现周围设备，现在用不到，以后可能会用
//                    manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//                        @Override
//                        public void onSuccess() {
//                            Toast.makeText(MainActivity.this,"Discovery Initiated",Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onFailure(int reason) {
//                            Toast.makeText(MainActivity.this,"Discovery Failed"+ reason ,Toast.LENGTH_SHORT).show();
//                        }
//                    });
//必须要调用addServiceRequest函数，而WifiP2pDnsSdServiceRequest.newInstance()中一定是service request for Bonjour，就必须要回调setDnsSdResponseListeners,
// 而不能调用setServiceResponseListener
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
//manager.findFragmentById(); 根据ID来找到对应的Fragment实例，主要用在静态添加fragment的布局中，因为静态添加的fragment才会有ID
//manager.findFragmentByTag();根据TAG找到对应的Fragment实例，主要用于在动态添加的fragment中，根据TAG来找到fragment实例
//                    获取响应的服务，并将获取的服务以列表的形式展现出来
//findFragementByTag();会重绘fragment,adapter.add后数据源没有变化，所以notifyDataSetChanged()函数不会执行。
                    manager.setDnsSdResponseListeners(channel, new WifiP2pManager.DnsSdServiceResponseListener() {
                        @Override
                        public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                           // deviceListFragment = (DeviceListFragment) getFragmentManager().findFragmentByTag("services");
                            deviceListFragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.list_frag);
                            //deviceListFragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.layout.device_list);
                            DeviceListFragment.WifiServiceAdapter adapter = (DeviceListFragment.WifiServiceAdapter)deviceListFragment.getListAdapter();
                            WifiDeviceWithLabel service = new WifiDeviceWithLabel(srcDevice,instanceName);
                            adapter.add(service);
                            count++;
                            Log.d(MainActivity.TRG,"adapter中的service的个数"+adapter.getCount()+"调用add次数"+String.valueOf(count));
//                            if(adapter.getCount()==0){
//                                adapter.clear();
//                            }
                            //adapter.notifyDataSetChanged();
                            Log.d(MainActivity.TRG, "onBonjourServiceAvailable "
                                    + instanceName);
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
    public void removeLocalService(){
        Map<String,String> record = new HashMap<String,String>();
        record.put(TXTRECORD_PROP_AVAILABLE,"visable");
        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(getInstanceName,serviceType,record);
        manager.removeLocalService(channel, wifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(MainActivity.TRG,"rrrrrrrrrrrrrrrrrrrrrrrrrrrremovelocalService successful");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(MainActivity.TRG,"Failed rrrrrrrrrrrremovelocalServiceeeeeeeeeeeeeeeeeee"+reason);
            }
        });
    }
    @Override
    public void publishService(String string){
        //现在理解的instanceName就是所发布的服务,那么问题就是如何将instanceName的内容显示到组标签中。
        String dyInstanceName = string;
        getInstanceName = dyInstanceName;
        Log.d(MainActivity.TRG,getInstanceName+"yiyiyiyiyiyiyiyiyiyiyiyi");
        Map<String,String> record = new HashMap<String,String>();
        record.put(TXTRECORD_PROP_AVAILABLE,"visable");
        WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(dyInstanceName,serviceType,record);
        manager.addLocalService(channel, wifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(MainActivity.TRG,"addlocalService successfullllllllllllllllllll");
            }

            @Override
            public void onFailure(int reason) {
                    Log.d(MainActivity.TRG,"Failed addlocalServiceeeeeeeeeeeeeeeeeee"+reason);
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
