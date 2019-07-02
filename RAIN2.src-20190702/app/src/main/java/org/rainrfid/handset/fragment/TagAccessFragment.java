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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.rainrfid.handset.MainActivity;
import org.rainrfid.handset.MyToast;
import org.rainrfid.handset.R;
import org.rainrfid.lib.Common;

public class TagAccessFragment extends Fragment {

    private EditText editTextAccess, editTextStart, editTextLength, editTextValue;
    private Spinner spinnerBank;
    private CheckBox checkBoxHex;
    private Button buttonRead, buttonWrite, buttonClear;
    private Spinner spinnerArea, spinnerAction;
    private Button buttonLock;
    private EditText editTextKill;
    private Button buttonKill;

    private MainActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_access, container, false);
        editTextAccess = view.findViewById(R.id.edit_text_access);
        String[] bank = {"RFU", "EPC", "TID", "User"};
        ArrayAdapter<String> adapterBank = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, bank);
        spinnerBank = view.findViewById(R.id.spinner_bank);
        spinnerBank.setAdapter(adapterBank);
        spinnerBank.setSelection(1);
        editTextStart = view.findViewById(R.id.edit_text_start);
        editTextLength = view.findViewById(R.id.edit_text_length);
        editTextValue = view.findViewById(R.id.edit_text_value);
        checkBoxHex = view.findViewById(R.id.check_box_hex);
        buttonRead = view.findViewById(R.id.button_read);
        buttonWrite = view.findViewById(R.id.button_write);
        buttonClear = view.findViewById(R.id.button_access_clear);
        buttonRead.setOnClickListener(onClickListener);
        buttonWrite.setOnClickListener(onClickListener);
        buttonClear.setOnClickListener(onClickListener);
        String[] lock_area = {"KILL PASSWORD", "ACCESS PASSWORD", "EPC BANK", "TID BANK", "USER BANK"};
        String[] lock_action = {"ACCESSIBLE", "ALWAYS_ACCESSIBLE", "SECURED_ACCESSIBLE", "ALWAYS_NOT_ACCESSIBLE", "NO_CHANGE"};
        ArrayAdapter<String> adapterArea = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, lock_area);
        ArrayAdapter<String> adapterAction = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, lock_action);
        spinnerArea = view.findViewById(R.id.spinner_lock_area);
        spinnerArea.setAdapter(adapterArea);
        spinnerAction = view.findViewById(R.id.spinner_lock_action);
        spinnerAction.setAdapter(adapterAction);
        buttonLock = view.findViewById(R.id.button_lock_tag);
        buttonLock.setOnClickListener(onClickListener);
        editTextKill = view.findViewById(R.id.edit_text_kill);
        buttonKill = view.findViewById(R.id.button_kill_tag);
        buttonKill.setOnClickListener(onClickListener);

        view.findViewById(R.id.button_read_temperature).setOnClickListener(onClickListener);

        setEnable(false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity) context;
    }

    public void setEnable(boolean enable) {
        buttonRead.setEnabled(enable);
        buttonWrite.setEnabled(enable);
        buttonLock.setEnabled(enable);
        buttonKill.setEnabled(enable);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_read:
                    try {
                        String access = editTextAccess.getText().toString();
                        String start = editTextStart.getText().toString();
                        String length = editTextLength.getText().toString();
                        if (access.length() == 0) {
                            MyToast.show(activity, R.string.toast_access_error);
                            editTextAccess.requestFocus();
                            break;
                        }
                        if (start.length() == 0) {
                            MyToast.show(activity, R.string.toast_start_error);
                            editTextStart.requestFocus();
                            break;
                        }
                        if (length.length() == 0){
                            MyToast.show(activity, R.string.toast_length_error);
                            editTextLength.requestFocus();
                            break;
                        }
                        byte[] access_password = Common.hex2Bytes(access);
                        int start_ = Integer.parseInt(start);
                        int length_ = Integer.parseInt(length);
                        byte[] read = activity.device.readTag(spinnerBank.getSelectedItemPosition(), start_, length_, access_password);
                        String value;
                        if (checkBoxHex.isChecked()) {
                            value = Common.bytes2String(read, 0, read.length);
                        } else {
                            value = new String(read);
                        }
                        editTextValue.setText(value);
                        MyToast.show(activity, R.string.toast_read_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_read_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_write:
                    try {
                        String access = editTextAccess.getText().toString();
                        String start = editTextStart.getText().toString();
                        String length = editTextLength.getText().toString();
                        String write = editTextValue.getText().toString();
                        if (access.length() == 0) {
                            MyToast.show(activity, R.string.toast_access_error);
                            editTextAccess.requestFocus();
                            break;
                        }
                        if (start.length() == 0) {
                            MyToast.show(activity, R.string.toast_start_error);
                            editTextStart.requestFocus();
                            break;
                        }
                        if (length.length() == 0){
                            MyToast.show(activity, R.string.toast_length_error);
                            editTextLength.requestFocus();
                            break;
                        }
                        byte[] access_password = Common.hex2Bytes(access);
                        int start_ = Integer.parseInt(start);
                        int length_ = Integer.parseInt(length);
                        byte[] write_bs;
                        if (checkBoxHex.isChecked()) {
                            write_bs = Common.hex2Bytes(write);
                        } else {
                            write_bs = write.getBytes();
                        }
                        activity.device.writeTag(spinnerBank.getSelectedItemPosition(), start_, length_, access_password, write_bs);
                        MyToast.show(activity, R.string.toast_write_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_write_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_access_clear:
                    editTextValue.setText(null);
                    break;
                case R.id.button_lock_tag:
                    try {
                        String value = editTextValue.getText().toString();
                        String access = editTextAccess.getText().toString();
                        if (value.length() == 0) {
                            MyToast.show(activity, R.string.toast_epc_error);
                            editTextValue.requestFocus();
                            break;
                        }
                        byte[] epc = Common.hex2Bytes(value);
                        byte[] access_password = Common.hex2Bytes(access);
                        activity.device.lockTag(access_password, epc, spinnerArea.getSelectedItemPosition(), spinnerAction.getSelectedItemPosition());
                        MyToast.show(activity, R.string.toast_lock_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_lock_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_kill_tag:
                    try {
                        String kill = editTextKill.getText().toString();
                        String value = editTextValue.getText().toString();
                        if (kill.length() == 0) {
                            MyToast.show(activity, R.string.toast_kill_error);
                            editTextKill.requestFocus();
                            break;
                        }
                        if (value.length() == 0) {
                            MyToast.show(activity, R.string.toast_epc_error);
                            editTextValue.requestFocus();
                            break;
                        }
                        byte[] kill_password = Common.hex2Bytes(kill);
                        byte[] epc = Common.hex2Bytes(value);
                        activity.device.killTag(kill_password, epc);
                        MyToast.show(activity, R.string.toast_kill_success);
                        activity.beep.playOk();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyToast.show(activity, R.string.toast_kill_failed);
                        activity.beep.playError();
                    }
                    break;
                case R.id.button_read_temperature:
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //String temperature = activity.device.readTemperature(null, true, 0);
                                //Log.d(activity.TAG, temperature);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.run();
                    break;
                default:
                    break;
            }
        }
    };
}
