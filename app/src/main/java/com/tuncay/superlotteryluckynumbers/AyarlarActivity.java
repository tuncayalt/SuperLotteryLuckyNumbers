package com.tuncay.superlotteryluckynumbers;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class AyarlarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar);

        String sevdigimKelime = getSevdigimKelime();
        EditText edtSevdigimKelimeAyarlar = (EditText) findViewById(R.id.edtSevdigimKelimeAyarlar);
        edtSevdigimKelimeAyarlar.setText(sevdigimKelime);
        edtSevdigimKelimeAyarlar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SharedPreferences sharedPref = getSharedPreferences("sevdigimKelime", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("kelime", s.toString());
                editor.apply();
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Boolean mesajIstek = getMesajIstek();
        Switch swMesajIstek = (Switch) findViewById(R.id.swMesajIstek);
        swMesajIstek.setChecked(mesajIstek);
        swMesajIstek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                SharedPreferences sharedPref = getSharedPreferences("firebaseUserToken", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                if(isChecked){
                    editor.putString("pushCekilis", "DT");
                }else{
                    editor.putString("pushCekilis", "DF");
                }
                editor.apply();
            }
        });
    }

    private Boolean getMesajIstek() {
        SharedPreferences sPref = getSharedPreferences("firebaseUserToken", MODE_PRIVATE);
        String pushCekilis = sPref.getString("pushCekilis", "RT");
        if (pushCekilis.substring(1,2).equals("F"))
            return false;
        return true;
    }

    private String getSevdigimKelime(){
        SharedPreferences sPref = getSharedPreferences("sevdigimKelime", MODE_PRIVATE);
        String sevdigimKelime = sPref.getString("kelime", "");
        return sevdigimKelime;
    }
}
