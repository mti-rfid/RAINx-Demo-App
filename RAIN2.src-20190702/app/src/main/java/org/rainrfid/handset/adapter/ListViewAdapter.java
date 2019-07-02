package org.rainrfid.handset.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.rainrfid.handset.R;
import org.rainrfid.handset.bean.EpcBean;

import java.util.concurrent.CopyOnWriteArrayList;

public class ListViewAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private CopyOnWriteArrayList<EpcBean> epcBeanArrayList = new CopyOnWriteArrayList<>();

    public ListViewAdapter(Context context) {
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setData(CopyOnWriteArrayList<EpcBean> epcBeans) {
        if (epcBeans == null) {
            epcBeanArrayList.clear();
        } else {
            epcBeanArrayList = (CopyOnWriteArrayList<EpcBean>) epcBeans.clone();
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return epcBeanArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return epcBeanArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_inventory, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewIndex = view.findViewById(R.id.text_view_index);
            viewHolder.textViewEpc = view.findViewById(R.id.text_view_epc);
            viewHolder.textViewCount = view.findViewById(R.id.text_view_count);
            view.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) view.getTag();
        viewHolder.textViewIndex.setText(String.valueOf(i + 1));
        viewHolder.textViewEpc.setText(epcBeanArrayList.get(i).getPc() + " " + epcBeanArrayList.get(i).getEpc());
        viewHolder.textViewCount.setText(String.valueOf(epcBeanArrayList.get(i).getCount()));
        return view;
    }

    class ViewHolder {
        public TextView textViewIndex, textViewEpc, textViewCount;
    }
}
