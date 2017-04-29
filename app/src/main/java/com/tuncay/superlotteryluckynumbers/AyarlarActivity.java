package com.tuncay.superlotteryluckynumbers;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class AyarlarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar);

        String sevdigimKelime = getSevdigimKelime();
        EditText edtSevdigimKelimeAyarlar = (EditText) findViewById(R.id.edtSevdigimKelimeAyarlar);
        edtSevdigimKelimeAyarlar.setText(sevdigimKelime);
    }

    private String getSevdigimKelime(){
        SharedPreferences sPref = getSharedPreferences("sevdigimKelime", MODE_PRIVATE);
        String sevdigimKelime = sPref.getString("kelime", "");
        return sevdigimKelime;
    }
}
