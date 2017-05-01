package com.tuncay.superlotteryluckynumbers.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.tuncay.superlotteryluckynumbers.R;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by mac on 3.04.2017.
 */
public class CustomSavedListAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private ArrayList<String> mData = new ArrayList<String>();
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

    private LayoutInflater mInflater;

    public CustomSavedListAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(final String item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(final String item) {
        mData.add(item);
        sectionHeader.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.saved_item, null);

                    holder.btnSavedSil = (Button) convertView.findViewById(R.id.btnSavedSil);
                    holder.btnSavedSil.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mData.remove(position);
                            notifyDataSetChanged();
                        }
                    });

                    holder.tvSaved = (TextView) convertView.findViewById(R.id.tvSaved);
                    holder.tvSavedWin = (TextView) convertView.findViewById(R.id.tvSavedWin);
                    break;
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.saved_header, null);
                    holder.tvSaved = (TextView) convertView.findViewById(R.id.textSeparator);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvSaved.setText(mData.get(position));

        return convertView;
    }

    public static class ViewHolder {
        public Button btnSavedSil;
        public TextView tvSaved;
        public TextView tvSavedWin;
    }

}
