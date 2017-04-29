package com.tuncay.superlotteryluckynumbers.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static com.google.android.gms.internal.zzt.TAG;

/**
 * Created by mac on 16.04.2017.
 */

public class MyFireBaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String REG_TOKEN = "REG_TOKEN";
    private FirebaseAuth mAuth;
    boolean error;
    HashMap<String, String> postDataParams;
    String urlSaveToken = "https://superlotteryluckynumbersserver.eu-gb.mybluemix.net/api/user/SaveToken";
    String urlSaveUser = "https://superlotteryluckynumbersserver.eu-gb.mybluemix.net/api/user/SaveUser";
    String recentToken;
    String userMail;
    String prevToken;
    String prevUser;
    Context context;
    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onTokenRefresh() {
        saveToken();
    }

    public MyFireBaseInstanceIDService() {
        this.context = this;
    }

    public void saveToken() {
        recentToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(REG_TOKEN, recentToken);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null){
            userMail = "";
        }else {
            userMail = mAuth.getCurrentUser().getEmail();
        }

        SharedPreferences sharedPref = this.getSharedPreferences("firebaseUserToken", MODE_PRIVATE);

        if (sharedPref != null){
            prevToken = sharedPref.getString("token", "");
            prevUser = sharedPref.getString("userMail", "");
        }
        else {
            prevToken = "";
            prevUser = "";
        }

        if (!recentToken.equals(prevToken)){
            sendRegistrationToServer(urlSaveToken);
        }
    }

    public void saveUserMail(Context context) {
        this.context = context;
        recentToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(REG_TOKEN, recentToken);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null){
            userMail = "";
        }else {
            userMail = mAuth.getCurrentUser().getEmail();
        }

        SharedPreferences sharedPref = context.getSharedPreferences("firebaseUserToken", MODE_PRIVATE);

        if (sharedPref != null){
            prevToken = sharedPref.getString("token", "");
            prevUser = sharedPref.getString("userMail", "");
        }
        else {
            prevToken = "";
            prevUser = "";
        }

        if (!userMail.equals(prevUser)){
            sendRegistrationToServer(urlSaveUser);
        }

    }

    public void sendRegistrationToServer(String url) {

        postDataParams = new HashMap<>();
        postDataParams.put("prev_token", prevToken);
        postDataParams.put("recent_token", recentToken);
        postDataParams.put("user_mail", userMail);
        postDataParams.put("prev_user_mail", prevUser);

        JsonPutTask task = new JsonPutTask();
        task.execute(url);

    }

    public  HttpURLConnection getHttpConnection(String url, String type){
        URL uri = null;
        HttpURLConnection con = null;
        try{
            uri = new URL(url);
            con = (HttpURLConnection) uri.openConnection();
            con.setRequestMethod(type); //type: POST, PUT, DELETE, GET
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setConnectTimeout(60000); //60 secs
            con.setReadTimeout(60000); //60 secs
            con.setRequestProperty("Content-Type", "application/json");
        }catch(Exception e){
            Log.d("FirebaseService", "connection i/o failed" );
        }


        return con;
    }

    private class JsonPutTask extends AsyncTask<String, Void, String> {

        ProgressDialog progress;

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            StringBuilder resultText = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("PUT");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));
                writer.write("");
                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        result+=line;
                    }
                }
                else {
                    result="";

                }
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
                return "";
            }
            catch (ProtocolException e) {
                e.printStackTrace();
                error = true;
                return "";
            }
            catch (IOException e) {
                e.printStackTrace();
                error = true;
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (error){
                Log.d(TAG, "Error on token change server update");
            }else {
                SharedPreferences sharedPref = context.getSharedPreferences("firebaseUserToken", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("token", recentToken);
                editor.putString("userMail", userMail);
                editor.apply();
            }

        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first) {
                first = false;
            }
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}