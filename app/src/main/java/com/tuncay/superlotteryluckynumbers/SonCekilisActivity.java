package com.tuncay.superlotteryluckynumbers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tuncay.superlotteryluckynumbers.constant.Constant;
import com.tuncay.superlotteryluckynumbers.model.Cekilis;
import com.tuncay.superlotteryluckynumbers.service.IServerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SonCekilisActivity extends AppCompatActivity {

    private static final String TAG = "SonCekilisActivity";
    boolean error;
    TextView txtCekilisTarihi;
    TextView txtCekilisNumaralar1;
    TextView txtCekilisNumaralar2;
    TextView txtCekilisNumaralar3;
    TextView txtCekilisNumaralar4;
    TextView txtCekilisNumaralar5;
    TextView txtCekilisNumaralar6;
    IServerService serverService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_son_cekilis);

        txtCekilisTarihi = (TextView) findViewById(R.id.txtCekilisTarihi);
        txtCekilisNumaralar1 = (TextView) findViewById(R.id.txtCekilisNumaralar1);
        txtCekilisNumaralar2 = (TextView) findViewById(R.id.txtCekilisNumaralar2);
        txtCekilisNumaralar3 = (TextView) findViewById(R.id.txtCekilisNumaralar3);
        txtCekilisNumaralar4 = (TextView) findViewById(R.id.txtCekilisNumaralar4);
        txtCekilisNumaralar5 = (TextView) findViewById(R.id.txtCekilisNumaralar5);
        txtCekilisNumaralar6 = (TextView) findViewById(R.id.txtCekilisNumaralar6);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.serverUrlBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serverService = retrofit.create(IServerService.class);

        FillCekilisBilgileri();

    }

    private void FillCekilisBilgileri() {
        JsonReadTask task = new JsonReadTask();
        task.execute();
    }

    // Async Task to access the web
    private class JsonReadTask extends AsyncTask<String, Void, String> {

        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(SonCekilisActivity.this);
            progress.setIndeterminate(true);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setMessage("Son çekiliş bilgilerini alıyor...");
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            try {
                Cekilis cekilis = serverService.getSonCekilis().execute().body();

                Gson gson = new Gson();

                result = gson.toJson(cekilis);

                error = false;

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return result;
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
                error = true;
                return null;
            }
            catch (ProtocolException e) {
                e.printStackTrace();
                error = true;
                return null;
            }
            catch (IOException e) {
                e.printStackTrace();
                error = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (error){
                String errorText = "İnternet bağlantısında hata";
                Toast toast = Toast.makeText(SonCekilisActivity.this, errorText, Toast.LENGTH_LONG);
                toast.show();
                SharedPreferences sharedPref = SonCekilisActivity.this.getPreferences(Context.MODE_PRIVATE);
                NumaraYaz(sharedPref.getString("cekilisJson", ""));
            }else {
                NumaraYaz(result);
                SharedPreferences sharedPref = SonCekilisActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("cekilisJson", result);
                editor.apply();
            }
            progress.dismiss();
        }
    }

    private void NumaraYaz(String jsonStr) {
        if (jsonStr != null) {
            try {
                JSONObject data = new JSONObject(jsonStr);
                String numaraString = data.getString("numbers");
                String tarihView = data.getString("tarih_view");

                String[] resultList = numaraString.split("-");
                txtCekilisNumaralar1.setText(resultList[0].trim());
                txtCekilisNumaralar2.setText(resultList[1].trim());
                txtCekilisNumaralar3.setText(resultList[2].trim());
                txtCekilisNumaralar4.setText(resultList[3].trim());
                txtCekilisNumaralar5.setText(resultList[4].trim());
                txtCekilisNumaralar6.setText(resultList[5].trim());

                txtCekilisTarihi.setText(tarihView);
            }
            catch (final JSONException e) {
                //Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Internet bağlantı hatası (jsonEx): " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

        }

    }


}
