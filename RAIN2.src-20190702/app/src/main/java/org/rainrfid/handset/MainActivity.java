package org.rainrfid.handset;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.rainrfid.handset.R;
import org.rainrfid.handset.adapter.BarcodeListViewAdapter;
import org.rainrfid.handset.adapter.FragmentAdapter;
import org.rainrfid.handset.adapter.ListViewAdapter;
import org.rainrfid.handset.bean.BarcodeBean;
import org.rainrfid.handset.bean.EpcBean;
import org.rainrfid.handset.fragment.InventoryFragment;
import org.rainrfid.handset.fragment.SettingsFragment;
import org.rainrfid.handset.fragment.TagAccessFragment;
import org.rainrfid.lib.Device;
import org.rainrfid.lib.OnBarcodeListener;
import org.rainrfid.lib.OnConnectionListener;
import org.rainrfid.lib.OnHandleListener;
import org.rainrfid.lib.OnUHFListener;
import org.rainrfid.lib.OnUpgradeListener;
import org.rainrfid.lib.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private SharedPreferences sharedPreferences;

    public BluetoothAdapter bluetoothAdapter;
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_FINE_LOCATION = 0;
    public static final int REQUEST_BT = 2;
    public static final int REQUEST_FILE = 3;
    public static final String BT_ADDRESS = "bt_address";
    private static boolean intent_ret;
    private String bluetoothAddress;

    public Device device;
    public Beep beep;

    private InventoryFragment inventory;
    private SettingsFragment settings;
    private TagAccessFragment access;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public static CopyOnWriteArrayList<EpcBean> epcBeanArrayList = new CopyOnWriteArrayList<>();
    public static List<String> index = new LinkedList<>();
    public ListViewAdapter listViewAdapter;

    public long last_cnt;
    public long cnt;

    private LinearLayout linearLayoutUhf, linearLayoutBarcode, linearLayoutSettings;
    private int frame_idx;

    private BarcodeListViewAdapter barcodeListViewAdapter;
    private ArrayList<BarcodeBean> barcodeBeans = new ArrayList<>();

    private TextView textViewAppV, textViewFirmV;
    private MenuItem menuItemReconnect;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.remove("android:support:fragments");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        Toolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
            return;
        }

        device = Device.getInstance(this);
        device.setOnConnectionListener(onConnectionListener);
        device.setOnUHFListener(onUHFListener);
        device.setOnBarcodeListener(onBarcodeListener);
        device.setOnUpgradeListener(onUpgradeListener);
        device.setOnHandleListener(onHandleListener);

        device.setDebug(true);

        beep = new Beep(this);
        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);

        findViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (device != null) {
            device.setOnConnectionListener(onConnectionListener);
        }
        if (!intent_ret) {
            checkLocationEnable();
        }
        Log.d(TAG, "resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (device != null) {
            handler.removeMessages(MSG_SPEED);
            device.setOnConnectionListener(null);
            device.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult : " + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION && grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            checkLocationEnable();
        } else if (requestCode == REQUEST_FILE && grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode != Activity.RESULT_OK) {
            checkBluetooth();
        } else if (requestCode == REQUEST_BT) {
            if (resultCode == RESULT_OK) {
                bluetoothAddress = data.getStringExtra(BT_ADDRESS);
                if (!StringUtils.isEmpty(bluetoothAddress)) {
                    Log.d(TAG, bluetoothAddress);
                    intent_ret = true;
                    connect();
                }
            } else {
                intent_ret = false;
            }
        }
        Log.d(TAG, "onActivityResult");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menuItemReconnect = menu.getItem(0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search_bt) {
            startActivityForResult(new Intent(MainActivity.this, SearchActivity.class), REQUEST_BT);
        } else if (item.getItemId() == R.id.action_reconnect) {
            connect();
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkLocationEnable() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (isLocationOpen(this)) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
                    }, REQUEST_FINE_LOCATION);
                    return;
                }
            }
        }
        checkBluetooth();
    }

    public boolean isLocationOpen(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider || isNetworkProvider;
    }

    public void checkBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            checkBluetoothEnable();
        }
    }

    public void checkBluetoothEnable() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {

            bluetoothAddress = sharedPreferences.getString(BT_ADDRESS, "");
            if (StringUtils.isEmpty(bluetoothAddress)) {
                startActivityForResult(new Intent(this, SearchActivity.class), REQUEST_BT);
            } else {
                connect();
            }
        }
    }

    private void connect() {
        if (device != null && !StringUtils.isEmpty(bluetoothAddress)) {
            device.connect(bluetoothAddress);
            showLoading(R.string.dialog_connect);
        }
    }

    private void setEnable(boolean enable) {
        if (inventory != null) {
            inventory.setEnable(enable);
        }
        if (settings != null) {
            settings.setEnable(enable);
        }
        if (access != null) {
            access.setEnable(enable);
        }
    }


    private AlertDialog dialog;
    private void showLoading(int msgId) {
        hideLoading();
        View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        final TextView textViewMessage = view.findViewById(R.id.text_view_message);
        textViewMessage.setText(msgId);
        dialog = new AlertDialog.Builder(this).setView(view).setCancelable(false).show();
    }

    private void hideLoading() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void findViews() {
        listViewAdapter = new ListViewAdapter(this);
        listViewAdapter.setData(epcBeanArrayList);
        linearLayoutUhf = findViewById(R.id.linear_layout_uhf);
        linearLayoutBarcode = findViewById(R.id.linear_layout_bar);
        linearLayoutSettings = findViewById(R.id.linear_layout_settings);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        inventory = new InventoryFragment();
        settings = new SettingsFragment();
        access = new TagAccessFragment();
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(inventory);
        fragmentList.add(settings);
        fragmentList.add(access);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragmentList);

        viewPager = findViewById(R.id.view_pager_main);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(fragmentAdapter);
        tabLayout = findViewById(R.id.tab_main);

        viewPager.addOnPageChangeListener(onPageChangeListener);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        ListView listViewBarcode = findViewById(R.id.list_view_barcode);
        barcodeListViewAdapter = new BarcodeListViewAdapter(this);
        listViewBarcode.setAdapter(barcodeListViewAdapter);

        findViewById(R.id.button_barcode).setOnClickListener(onClickListener);
        findViewById(R.id.button_barcode_clear).setOnClickListener(onClickListener);

        textViewAppV = findViewById(R.id.text_view_app_v);
        textViewAppV.setText(AppVersion.get(this));
        textViewFirmV = findViewById(R.id.text_view_firm_v);
        textViewFirmV.setOnClickListener(onClickListener);
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            tabLayout.getTabAt(position).select();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_uhf:
                    if (linearLayoutUhf.getVisibility() != View.VISIBLE) {
                        linearLayoutUhf.setVisibility(View.VISIBLE);
                    }
                    linearLayoutBarcode.setVisibility(View.GONE);
                    linearLayoutSettings.setVisibility(View.GONE);
                    frame_idx = 0;
                    return true;
                case R.id.navigation_barcode:
                    if (linearLayoutBarcode.getVisibility() != View.VISIBLE) {
                        linearLayoutBarcode.setVisibility(View.VISIBLE);
                    }
                    linearLayoutUhf.setVisibility(View.GONE);
                    linearLayoutSettings.setVisibility(View.GONE);
                    frame_idx = 1;
                    return true;
                case R.id.navigation_settings:
                    if (linearLayoutSettings.getVisibility() != View.VISIBLE) {
                        linearLayoutSettings.setVisibility(View.VISIBLE);
                    }
                    linearLayoutUhf.setVisibility(View.GONE);
                    linearLayoutBarcode.setVisibility(View.GONE);
                    frame_idx = 2;
                    return true;
            }
            return false;
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_barcode:
                    device.scanBarcode();
                    findViewById(R.id.button_barcode).setEnabled(false);
                    break;
                case R.id.button_barcode_clear:
                    barcodeBeans.clear();
                    barcodeListViewAdapter.setData(null);
                    break;
                case R.id.text_view_firm_v:
                    upgrade();
                    break;
            }
        }
    };

    final int MSG_SPEED = 1000;
    long speed_tick;

    Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case MSG_SPEED:
                    long tick = System.currentTimeMillis();
                    sendEmptyMessageDelayed(MSG_SPEED, 1000);
                    inventory.setSpeed((cnt - last_cnt) * 1000 / (tick - speed_tick));
                    last_cnt = cnt;
                    speed_tick = tick;
                    break;
                default:
                    break;
            }
        }
    };

    private OnConnectionListener onConnectionListener = new OnConnectionListener() {
        @Override
        public void onConnecting() {
            Log.d(TAG, "onConnecting");
        }

        @Override
        public void onConnected() {
            Log.d(TAG, "onConnected");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    intent_ret = false;

                    menuItemReconnect.setVisible(false);
                    hideLoading();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(BT_ADDRESS, bluetoothAddress).commit();

                    try {
                        String version = device.getVersion();
                        textViewFirmV.setText(version);
                        Log.d(TAG, version);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    boolean uhf = false;

                    try {
                        device.uhfPowerOn();
                        uhf = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    setEnable(uhf);
                    if (inventory != null) {
                        findViewById(R.id.button_stop).setEnabled(uhf);
                    }

                    boolean barcode = false;
                    try {
                        device.barcodePowerOn();
                        barcode = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    findViewById(R.id.button_barcode).setEnabled(barcode);

                    if (uhf) {
                        if (settings != null) {
                            settings.getSettings();
                        }
                    }
                }
            });
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "onDisconnected");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideLoading();
                    new AlertDialog.Builder(MainActivity.this).setTitle(R.string.dialog_reconnect).setTitle(R.string.dialog_title).setMessage(R.string.dialog_reconnect).setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setEnable(false);
                            menuItemReconnect.setVisible(true);
                        }
                    }).setPositiveButton(R.string.dialog_button_reconnect, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            connect();
                        }
                    }).setCancelable(false).show();
                }
            });
        }
    };

    volatile long play_tick;
    private OnUHFListener onUHFListener = new OnUHFListener() {

        @Override
        public void onStart() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyToast.show(MainActivity.this, R.string.toast_inventory);
                    handler.sendEmptyMessageDelayed(MSG_SPEED, 1000);
                    speed_tick = System.currentTimeMillis();
                    setEnable(false);
                }
            });
        }

        @Override
        public void onInventory(String pc, String epc, double rssi) {
            cnt++;
            final int idx = index.indexOf(epc);
            if (idx > -1) {
                epcBeanArrayList.get(idx).setRssi(rssi);
                epcBeanArrayList.get(idx).setCount(epcBeanArrayList.get(idx).getCount() + 1);
            } else {
                EpcBean epcBean = new EpcBean();
                epcBean.setPc(pc);
                epcBean.setEpc(epc);
                epcBean.setRssi(rssi);
                epcBean.setCount(1);

                epcBeanArrayList.add(epcBean);
                index.add(epc);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listViewAdapter.setData(epcBeanArrayList);
                    if (idx == -1) {
                        inventory.setUnique();
                    }
                    if (System.currentTimeMillis() - play_tick > 160) {
                        beep.playInv();
                        play_tick = System.currentTimeMillis();
                    }
                }
            });
        }

        @Override
        public void onStop() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handler.removeMessages(MSG_SPEED);
                    MyToast.show(MainActivity.this, R.string.toast_inventory_stop);
                    setEnable(true);
                }
            });
        }
    };

    private OnBarcodeListener onBarcodeListener = new OnBarcodeListener() {
        @Override
        public void onBarcode(String barcode) {
            boolean found = false;
            for (BarcodeBean barcodeBean : barcodeBeans) {
                if (barcode.equalsIgnoreCase(barcodeBean.getBarcode())) {
                    barcodeBean.setCount(barcodeBean.getCount() + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                BarcodeBean barcodeBean = new BarcodeBean();
                barcodeBean.setBarcode(barcode);
                barcodeBean.setCount(1);
                barcodeBeans.add(barcodeBean);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    barcodeListViewAdapter.setData(barcodeBeans);
                    beep.playInv();
                    findViewById(R.id.button_barcode).setEnabled(true);
                }
            });
        }

        @Override
        public void onFailed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    beep.playError();
                    findViewById(R.id.button_barcode).setEnabled(true);
                }
            });
        }
    };


    public void upgrade() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_FILE);
            return;
        }

        final String directory = Environment.getExternalStorageDirectory() + "/";
        final String[] files = new File(directory).list();
        if (files != null) {
            new AlertDialog.Builder(this).setItems(files, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        device.upgrade(directory + files[i]);
                    } catch (IOException e) {
                        e.printStackTrace();
                        MyToast.show(MainActivity.this, R.string.upgrade_failed);
                    }
                }
            }).show();
        }
    }

    private OnUpgradeListener onUpgradeListener = new OnUpgradeListener() {
        @Override
        public void onUpgrade(final int progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewFirmV.setText(progress + "%");
                }
            });
        }

        @Override
        public void onUpgradeResult(final boolean success) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyToast.show(MainActivity.this, success ? R.string.upgrade_success : R.string.upgrade_failed);
                }
            });
        }
    };

    private OnHandleListener onHandleListener = new OnHandleListener() {
        @Override
        public void onDown() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (frame_idx == 0) {
                        if (device != null) {
                            device.inventory();
                        }
                    } else if (frame_idx == 1) {
                        if (device != null) {
                            device.scanBarcode();
                            findViewById(R.id.button_barcode).setEnabled(false);
                        }
                    }
                }
            });
        }

        @Override
        public void onUp() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (frame_idx == 0) {
                        if (device != null) {
                            device.stopInventory();
                        }
                    }
                }
            });
        }
    };
}
