package org.rainrfid.handset.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import org.rainrfid.handset.MainActivity;
import org.rainrfid.handset.MyToast;
import org.rainrfid.handset.R;
import org.rainrfid.lib.AntiCollision;
import org.rainrfid.lib.Query;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsFragment extends Fragment {

    private MainActivity activity;
    private Spinner spinnerPower;
    private Button buttonPowerGet, buttonPowerSet;
    private RadioButton radioButtonFixed, radioButtonDynamic;
    private Spinner spinnerStartQ, spinnerMaxQ, spinnerMinQ;
    private Button buttonGetQ, buttonSetQ;
    private LinearLayout linearLayoutStart, linearLayoutMax, linearLayoutMin;
    private Spinner spinnerSession, spinnerQ, spinnerTarget, spinnerModulation;
    private Button buttonGetQuery, buttonSetQuery;
    private Spinner spinnerRegion, spinnerChannel;
    private Button buttonRegionGet, buttonRegionSet, buttonChannelGet, buttonChannelSet;
    private Button buttonUpgradeRegistry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        spinnerPower = view.findViewById(R.id.spinner_power);
        ArrayList<String> power = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            power.add(String.valueOf(30 - i));
        }
        ArrayAdapter<String> adapterP = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, power);
        spinnerPower.setAdapter(adapterP);
        buttonPowerGet = view.findViewById(R.id.button_get_power);
        buttonPowerSet = view.findViewById(R.id.button_set_power);
        buttonPowerGet.setOnClickListener(onClickListener);
        buttonPowerSet.setOnClickListener(onClickListener);

        radioButtonFixed = view.findViewById(R.id.radio_fixed);
        radioButtonDynamic = view.findViewById(R.id.radio_dynamic);

        linearLayoutStart = view.findViewById(R.id.linear_start);
        linearLayoutMax = view.findViewById(R.id.linear_max);
        linearLayoutMin = view.findViewById(R.id.linear_min);

        radioButtonFixed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                linearLayoutStart.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
                linearLayoutMax.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
                linearLayoutMin.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
            }
        });


        ArrayList<String> q = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            q.add("Q" + i);
        }
        ArrayAdapter<String> adapterQ = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, q);
        spinnerStartQ = view.findViewById(R.id.spinner_start_q);
        spinnerMaxQ = view.findViewById(R.id.spinner_max_q);
        spinnerMinQ = view.findViewById(R.id.spinner_min_q);
        spinnerStartQ.setAdapter(adapterQ);
        spinnerMaxQ.setAdapter(adapterQ);
        spinnerMinQ.setAdapter(adapterQ);

        buttonGetQ = view.findViewById(R.id.button_get_q);
        buttonSetQ = view.findViewById(R.id.button_set_q);
        buttonGetQ.setOnClickListener(onClickListener);
        buttonSetQ.setOnClickListener(onClickListener);

        String[] session = {"S0", "S1", "S2", "S3"};
        ArrayAdapter<String> adapterSession = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, session);
        spinnerSession = view.findViewById(R.id.spinner_session);
        spinnerSession.setAdapter(adapterSession);
        spinnerQ = view.findViewById(R.id.spinner_q);
        spinnerQ.setAdapter(adapterQ);

        String[] target = {"A", "B"};
        ArrayAdapter<String> adapterTarget = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, target);
        spinnerTarget = view.findViewById(R.id.spinner_target);
        spinnerTarget.setAdapter(adapterTarget);

        String[] modulation = {"M1", "M2", "M4", "M8"};
        ArrayAdapter<String> adapterModulation = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, modulation);
        spinnerModulation = view.findViewById(R.id.spinner_m);
        spinnerModulation.setAdapter(adapterModulation);
        buttonGetQuery = view.findViewById(R.id.button_get_query);
        buttonSetQuery = view.findViewById(R.id.button_set_query);
        buttonGetQuery.setOnClickListener(onClickListener);
        buttonSetQuery.setOnClickListener(onClickListener);

        String[] region = {"Korea", "USWide", "USNarrow", "Europe", "Japan", "China2", "Brazil1"};
        ArrayAdapter<String> adapterRegion = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, region);
        spinnerRegion = view.findViewById(R.id.spinner_region);
        spinnerRegion.setAdapter(adapterRegion);
        spinnerChannel = view.findViewById(R.id.spinner_channel);
        buttonRegionGet = view.findViewById(R.id.button_region_get);
        buttonRegionSet = view.findViewById(R.id.button_region_set);
        buttonChannelGet = view.findViewById(R.id.button_channel_get);
        buttonChannelSet = view.findViewById(R.id.button_channel_set);
        buttonRegionGet.setOnClickListener(onClickListener);
        buttonRegionSet.setOnClickListener(onClickListener);
        buttonChannelGet.setOnClickListener(onClickListener);
        buttonChannelSet.setOnClickListener(onClickListener);

        buttonUpgradeRegistry = view.findViewById(R.id.button_save);
        buttonUpgradeRegistry.setOnClickListener(onClickListener);

        setEnable(false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity) context;
    }

    public void getSettings() {
        try {
            getPower();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            getAntiCollision();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            getQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            getRegion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_get_power:
                    try {
                        getPower();
                        MyToast.show(activity, R.string.toast_get_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_get_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_set_power:
                    int power = 30 - spinnerPower.getSelectedItemPosition();
                    try {
                        activity.device.setPower(power * 10);
                        MyToast.show(activity, R.string.toast_set_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_set_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_get_q:
                    try {
                        getAntiCollision();
                        MyToast.show(activity, R.string.toast_get_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_get_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_set_q:
                    AntiCollision antiCollision = new AntiCollision();
                    if (radioButtonFixed.isChecked()) {
                        antiCollision.setMode(0);
                    } else {
                        antiCollision.setMode(1);
                        antiCollision.setStart(spinnerStartQ.getSelectedItemPosition());
                        antiCollision.setMax(spinnerMaxQ.getSelectedItemPosition());
                        antiCollision.setMin(spinnerMinQ.getSelectedItemPosition());
                    }
                    try {
                        activity.device.setAntiCollision(antiCollision);
                        MyToast.show(activity, R.string.toast_set_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_set_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_get_query:
                    try {
                        getQuery();
                        MyToast.show(activity, R.string.toast_get_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_get_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_set_query:
                    Query query = new Query();
                    query.setSession(spinnerSession.getSelectedItemPosition());
                    query.setQ(spinnerQ.getSelectedItemPosition());
                    query.setTarget(spinnerTarget.getSelectedItemPosition());
                    query.setM(spinnerModulation.getSelectedItemPosition());
                    query.setDR(1);
                    query.setTR(0);
                    query.setSel(0);
                    try {
                        activity.device.setQuery(query);
                        MyToast.show(activity, R.string.toast_set_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_set_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_region_get:
                    try {
                        getRegion();
                        MyToast.show(activity, R.string.toast_get_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_get_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_region_set:
                    try {
                        activity.device.setRegion(spinnerRegion.getSelectedItemPosition());
                        MyToast.show(activity, R.string.toast_set_success);
                        activity.beep.playOk();

                        initChannelTable();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_set_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_channel_get:
                    try {
                        getChannel();
                        MyToast.show(activity, R.string.toast_get_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_get_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_channel_set:
                    try {
                        activity.device.setChannel(Integer.parseInt(spinnerChannel.getSelectedItem().toString()));
                        MyToast.show(activity, R.string.toast_set_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_set_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_save:
                    try {
                        activity.device.updateRegistry();
                        MyToast.show(activity, R.string.toast_set_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_set_failed);
                        activity.beep.playError();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void getPower() throws Exception {
        int power = activity.device.getPower();
        spinnerPower.setSelection(30 - power / 10);
    }

    private void getAntiCollision() throws Exception {
        AntiCollision antiCollision = activity.device.getAntiCollision();
        if (antiCollision.getMode() == 0) {
            radioButtonFixed.setChecked(true);
        } else {
            radioButtonDynamic.setChecked(true);
            if (antiCollision.getStart() >= 0 && antiCollision.getStart() <= 15) {
                spinnerStartQ.setSelection(antiCollision.getStart());
            }
            if (antiCollision.getMax() >= 0 && antiCollision.getMax() <= 15) {
                spinnerMaxQ.setSelection(antiCollision.getMax());
            }
            if (antiCollision.getMin() >= 0 && antiCollision.getMin() <= 15) {
                spinnerMinQ.setSelection(antiCollision.getMin());
            }
        }
    }

    private void getQuery() throws Exception {
        Query query = activity.device.getQuery();
        if (query.getSession() >= 0 && query.getSession() <= 3) {
            spinnerSession.setSelection(query.getSession());
        }
        if (query.getQ() >= 0 && query.getQ() <= 15) {
            spinnerQ.setSelection(query.getQ());
        }
        if (query.getTarget() >= 0 && query.getTarget() <= 1) {
            spinnerTarget.setSelection(query.getTarget());
        }
        if (query.getM() >= 0 && query.getM() <= 3) {
            spinnerModulation.setSelection(query.getM());
        }
    }

    private void getRegion() throws Exception {
        int region = activity.device.getRegion();
        spinnerRegion.setSelection(region);
        initChannelTable();
    }

    private void initChannelTable() {
        try {
            byte[] channelTable = activity.device.getChannelTable();
            Arrays.sort(channelTable);
            ArrayList<String> channels = new ArrayList<>(channelTable.length);
            for (byte b : channelTable) {
                channels.add(String.valueOf(b & 0xff));
            }
            ArrayAdapter<String> adapterChannels = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, channels);
            spinnerChannel.setAdapter(adapterChannels);
            getChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getChannel() throws Exception {
        int channel = activity.device.getChannel();
        for (int i = 0; i < spinnerChannel.getAdapter().getCount(); i++) {
            if (spinnerChannel.getAdapter().getItem(i).toString().equalsIgnoreCase(channel + "")) {
                spinnerChannel.setSelection(i);
                break;
            }
        }
    }

    public void setEnable(boolean enable) {
        buttonPowerGet.setEnabled(enable);
        buttonPowerSet.setEnabled(enable);
        buttonGetQ.setEnabled(enable);
        buttonSetQ.setEnabled(enable);
        buttonGetQuery.setEnabled(enable);
        buttonSetQuery.setEnabled(enable);
        buttonRegionGet.setEnabled(enable);
        buttonRegionSet.setEnabled(enable);
        buttonChannelSet.setEnabled(enable);
        buttonChannelGet.setEnabled(enable);
        buttonUpgradeRegistry.setEnabled(enable);
    }
}
