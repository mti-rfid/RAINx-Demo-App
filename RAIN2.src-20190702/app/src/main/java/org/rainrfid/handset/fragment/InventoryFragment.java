package org.rainrfid.handset.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.rainrfid.handset.MainActivity;
import org.rainrfid.handset.MyToast;
import org.rainrfid.handset.R;
import org.rainrfid.lib.Common;

public class InventoryFragment extends Fragment {

    private MainActivity activity;
    private ListView listView;
    private TextView textViewUnique, textViewSpeed;
    private Button buttonInventory, buttonStop, buttonClear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        listView = view.findViewById(R.id.list_view_inventory);
        listView.setAdapter(activity.listViewAdapter);

        textViewUnique = view.findViewById(R.id.text_view_unique);
        textViewSpeed = view.findViewById(R.id.text_view_speed);

        buttonInventory = view.findViewById(R.id.button_inventory);
        buttonStop = view.findViewById(R.id.button_stop);
        buttonClear = view.findViewById(R.id.button_clear);

        buttonInventory.setOnClickListener(onClickListener);
        buttonStop.setOnClickListener(onClickListener);
        buttonClear.setOnClickListener(onClickListener);

        view.findViewById(R.id.linear_tag).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                select(null);
                return true;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                select(activity.epcBeanArrayList.get(i).getEpc());
                return true;
            }
        });

        setUnique();
        setSpeed(0);

        setEnable(false);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity) context;
    }

    public void setEnable(boolean enable) {
        buttonInventory.setEnabled(enable);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_inventory:
                    activity.device.inventory();
                    break;
                case R.id.button_stop:
                    activity.device.stopInventory();
                    break;
                case R.id.button_clear:
                    synchronized (activity) {
                        MainActivity.epcBeanArrayList.clear();
                        MainActivity.index.clear();
                        activity.listViewAdapter.setData(null);
                        activity.cnt = 0;
                        activity.last_cnt = 0;
                        setUnique();
                        setSpeed(0);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void select(final String epc) {
        new AlertDialog.Builder(activity).setItems(R.array.array_select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    if (epc == null || epc.length() == 0) {
                        return;
                    }
                    byte[] mask = Common.hex2Bytes(epc);
                    try {
                        activity.device.select(1, 4, mask);
                        MyToast.show(activity, R.string.toast_select);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_select_failed);
                        activity.beep.playError();
                    }
                } else if (i == 1) {
                    try {
                        activity.device.deselect();
                        MyToast.show(activity, R.string.toast_deselect);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_deselect_failed);
                        activity.beep.playError();
                    }
                }
            }
        }).show();
    }

    public void setUnique() {
        textViewUnique.setText(getString(R.string.text_view_unique, activity.index.size()));
    }

    public void setSpeed(long speed) {
        textViewSpeed.setText(getString(R.string.text_view_speed, speed));
    }
}
