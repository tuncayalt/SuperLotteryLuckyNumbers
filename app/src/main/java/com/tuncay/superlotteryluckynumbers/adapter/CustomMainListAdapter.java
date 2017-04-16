package com.tuncay.superlotteryluckynumbers.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tuncay.superlotteryluckynumbers.R;
import com.tuncay.superlotteryluckynumbers.model.MainListElement;

import java.util.ArrayList;

/**
 * Created by mac on 2.01.2017.
 */
public class CustomMainListAdapter extends ArrayAdapter<MainListElement> {
    Context context;
    ArrayList<MainListElement> elements = null;


    public CustomMainListAdapter(Context context, ArrayList<MainListElement> elements) {
        super(context, R.layout.list_content, elements);
        this.context = context;
        this.elements = elements;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_content, parent, false);
        Button btn = (Button) convertView.findViewById(R.id.btnListedenSil);
        btn.setTag(position);
        TextView sayilar = (TextView) convertView.findViewById(R.id.txtSayilar);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.chkOyna);
        cb.setTag(position);
        sayilar.setText(elements.get(position).getNumString());
        cb.setChecked(elements.get(position).getOyna());

        return convertView;
    }
}
