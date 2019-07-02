package org.rainrfid.handset.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.rainrfid.handset.R;
import org.rainrfid.handset.bean.BarcodeBean;

import java.util.ArrayList;

public class BarcodeListViewAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    private ArrayList<BarcodeBean> barcodeBeanArrayList = new ArrayList<>();

    public BarcodeListViewAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<BarcodeBean> barcodeBeans) {
        if (barcodeBeans == null) {
            this.barcodeBeanArrayList.clear();
        } else {
            this.barcodeBeanArrayList = (ArrayList<BarcodeBean>) barcodeBeans.clone();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return barcodeBeanArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return barcodeBeanArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = layoutInflater.inflate(R.layout.item_barcode, viewGroup, false);
            viewHolder.textViewIndex = view.findViewById(R.id.text_view_id);
            viewHolder.textViewBarcode = view.findViewById(R.id.text_view_barcode);
            viewHolder.textViewCount = view.findViewById(R.id.text_view_count);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.textViewIndex.setText(String.valueOf(i + 1));
        viewHolder.textViewBarcode.setText(barcodeBeanArrayList.get(i).getBarcode());
        viewHolder.textViewCount.setText(String.valueOf(barcodeBeanArrayList.get(i).getCount()));
        return view;
    }

    class ViewHolder {
        public TextView textViewIndex;

        public TextView textViewBarcode;

        public TextView textViewCount;
    }
}
