package com.tuncay.superlotteryluckynumbers.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by mac on 17.04.2017.
 */

public class MyHttpHandler {

    Context context;
    String progressMessage;
    String urlString;
    String method;
    boolean error;
    long fakeWaitMillis;
    Callable onErrorFunc;
    Callable onSuccessFunc;
    String jsonString;

    public MyHttpHandler(Context context, String progressMessage, String urlString,
                         String method, long fakeWaitMillis, Callable onErrorFunc,
                         Callable onSuccessFunc, String jsonString){
        this.context = context;
        this.progressMessage = progressMessage;
        this.urlString = urlString;
        this.method = method;
        this.fakeWaitMillis = fakeWaitMillis;
        this.onErrorFunc = onErrorFunc;
        this.onSuccessFunc = onSuccessFunc;
        this.jsonString = jsonString;
    }

    public void execute(){
        JsonTask task = new JsonTask();
        task.execute();
    }


    public class JsonTask extends AsyncTask<String, Void, String> {

        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(context);
            if (method.equals("GET")){
                progress.setIndeterminate(true);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setMessage(progressMessage);
                progress.show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            StringBuilder resultText = new StringBuilder();
            try {
                URL url = new URL(urlString);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod(method);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                if(method.equals("POST") || method.equals("PUT")){
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(jsonString);
                    wr.flush();
                }
                int HttpResult = conn.getResponseCode();
                if (HttpResult == HttpsURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line ;
                    while ((line = br.readLine()) != null) {
                        resultText.append(line);
                    }
                    br.close();
                    error = false;
                } else {
                    System.out.println(conn.getResponseMessage());
                    error = true;
                }

                result = resultText.toString();

                try {
                    Thread.sleep(fakeWaitMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                error = true;
                return null;
            } catch (ProtocolException e) {
                e.printStackTrace();
                error = true;
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                error = true;
                return null;
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if (error) {
                try {
                    onErrorFunc.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    onSuccessFunc.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            progress.dismiss();
        }
    }
}