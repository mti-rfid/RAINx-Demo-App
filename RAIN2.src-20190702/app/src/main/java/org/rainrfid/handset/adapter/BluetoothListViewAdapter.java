package org.rainrfid.handset.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.rainrfid.handset.R;
import org.rainrfid.handset.bean.BluetoothName;

import java.util.ArrayList;

public class BluetoothListViewAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    private ArrayList<BluetoothName> names = new ArrayList<>();

    public BluetoothListViewAdapter(Context context) {
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<BluetoothName> names) {
        if (names == null) {
            this.names.clear();
        } else {
            this.names = (ArrayList<BluetoothName>) names.clone();
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int i) {
        return names.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_bt, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewName = view.findViewById(R.id.text_view_name);
            viewHolder.textViewAddress = view.findViewById(R.id.text_view_address);
            viewHolder.textViewStatus = view.findViewById(R.id.text_view_status);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.textViewName.setText(names.get(i).getName());
        viewHolder.textViewAddress.setText(names.get(i).getAddress());
        viewHolder.textViewStatus.setText(names.get(i).getStatus());
        return view;
    }

    class ViewHolder {
        public TextView textViewName, textViewAddress, textViewStatus;
    }
}
