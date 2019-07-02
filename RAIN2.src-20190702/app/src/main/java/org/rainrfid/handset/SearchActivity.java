package org.rainrfid.handset;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.rainrfid.handset.R;
import org.rainrfid.handset.adapter.BluetoothListViewAdapter;
import org.rainrfid.handset.bean.BluetoothName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = SearchActivity.class.getSimpleName();

    private BluetoothAdapter bluetoothAdapter;
    private ListView listViewBound, listViewSearch;
    private BluetoothListViewAdapter bluetoothListViewAdapterBound, bluetoothListViewAdapterSearch;
    private ArrayList<BluetoothName> boundNames, searchNames;
    private ProgressBar progressBarSearch;
    private LinearLayout linearLayoutBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        Toolbar toolbar = findViewById(R.id.toolbar_search);
        toolbar.setTitle(R.string.activity_title_search);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        boundNames = new ArrayList<>();
        searchNames = new ArrayList<>();
        bluetoothListViewAdapterBound = new BluetoothListViewAdapter(this);
        bluetoothListViewAdapterSearch = new BluetoothListViewAdapter(this);
        listViewBound = findViewById(R.id.list_view_bound);
        listViewSearch = findViewById(R.id.list_view_search);
        listViewBound.setAdapter(bluetoothListViewAdapterBound);
        listViewSearch.setAdapter(bluetoothListViewAdapterSearch);

        progressBarSearch = findViewById(R.id.progress_search);
        linearLayoutBound = findViewById(R.id.linear_layout_bound);

        listViewBound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sendResult(boundNames.get(i).getAddress());
            }
        });

        listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, searchNames.get(i).getAddress());
                /*
                BluetoothDevice mDevice = bluetoothAdapter.getRemoteDevice(searchNames.get(i).getAddress());
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    try {
                        Method mMethod = BluetoothDevice.class.getMethod("createBond");
                        mMethod.invoke(mDevice);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }*/
                sendResult(searchNames.get(i).getAddress());
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        registerReceiver();
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(broadcastReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        this.registerReceiver(broadcastReceiver, filter);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean bAddDeviceToList = true;
            String action = intent.getAction();
            Log.d(TAG, action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    for (BluetoothName name : searchNames) {
                        if (name.getAddress().equalsIgnoreCase(device.getAddress())) {
                            bAddDeviceToList = false;
                            break;
                        }
                    }

                    if (bAddDeviceToList) {
                        BluetoothName name = new BluetoothName();
                        name.setName(device.getName());
                        name.setAddress(device.getAddress());
                        name.setStatus("");
                        searchNames.add(name);
                        bluetoothListViewAdapterSearch.setData(searchNames);

                        setListViewHeight(listViewSearch);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBarSearch.setVisibility(View.INVISIBLE);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String address = device.getAddress();
                Iterator<BluetoothName> iterator = searchNames.iterator();
                while (iterator.hasNext()) {
                    BluetoothName name = iterator.next();
                    if (name.getAddress().equalsIgnoreCase(address)) {
                        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                            name.setStatus("");
                        } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                            name.setStatus(getString(R.string.bt_bond_ing));
                        } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                            iterator.remove();
                            listPairedBTDevice();
                        }
                        break;
                    }
                }
                bluetoothListViewAdapterSearch.setData(searchNames);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listPairedBTDevice();
        doDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (item.getItemId() == R.id.action_search) {
            doDiscovery();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doDiscovery() {
        if (bluetoothAdapter != null) {
            cancelDiscovery();
            listPairedBTDevice();
            searchNames.clear();
            bluetoothListViewAdapterSearch.setData(null);
            bluetoothAdapter.startDiscovery();
            progressBarSearch.setVisibility(View.VISIBLE);
        }
    }

    private void cancelDiscovery() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        progressBarSearch.setVisibility(View.INVISIBLE);
    }

    public void listPairedBTDevice() {
        boundNames.clear();
        bluetoothListViewAdapterBound.setData(null);
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice bluetoothDevice : devices) {
                BluetoothName name = new BluetoothName();
                name.setName(bluetoothDevice.getName());
                name.setAddress(bluetoothDevice.getAddress());
                name.setStatus("");
                boundNames.add(name);
            }
            bluetoothListViewAdapterBound.setData(boundNames);
            setListViewHeight(listViewBound);

            if (boundNames.size() == 0) {
                linearLayoutBound.setVisibility(View.GONE);
            }
        }
    }

    public void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public void sendResult(String address) {
        Intent intent = getIntent();
        intent.putExtra(MainActivity.BT_ADDRESS, address);
        setResult(RESULT_OK, intent);
        finish();
    }
}
