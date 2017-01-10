package com.tuncay.sansoyunlaritahminsonuc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.tuncay.sansoyunlaritahminsonuc.model.ListElement;

import java.util.ArrayList;

/**
 * Created by mac on 2.01.2017.
 */
public class CustomListAdapter extends ArrayAdapter<ListElement> {
    Context context;
    ArrayList<ListElement> elements = null;


    public CustomListAdapter(Context context, ArrayList<ListElement> elements) {
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
        sayilar.setText(elements.get(position).getNums());
        if(elements.get(position).getOyna())
            cb.setChecked(true);
        else
            cb.setChecked(false);
        return convertView;
    }
}
