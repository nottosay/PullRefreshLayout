package com.wally.view.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wally.view.demo.model.RefreshModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wally on 2017/9/22.
 */

public class MyAdapter extends BaseAdapter {

    private List<RefreshModel> datas;

    private LayoutInflater layoutInflater;

    public MyAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void setDatas(List<RefreshModel> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    public void addDatas(List<RefreshModel> newDatas) {
        if (datas == null){
            datas = new ArrayList<>();
        }
        this.datas.addAll(newDatas);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas == null ? null : datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_simple, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.textView = view.findViewById(R.id.tv_simple);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.textView.setText(datas.get(i).title);
        return view;
    }

    class ViewHolder {
        public TextView textView;
    }
}
