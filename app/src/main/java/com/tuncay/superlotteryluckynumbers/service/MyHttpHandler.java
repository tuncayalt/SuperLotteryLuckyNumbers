package com.tuncay.superlotteryluckynumbers.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.Pair;

import java.io.BufferedWriter;
import java.io.IOException;
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
    Pair<String, String>[] parameters;

    public MyHttpHandler(Context context, String progressMessage, String urlString,
                         String method, long fakeWaitMillis, Pair<String, String>[] parameters, Callable onErrorFunc,
                         Callable onSuccessFunc){
        this.context = context;
        this.progressMessage = progressMessage;
        this.urlString = urlString;
        this.method = method;
        this.fakeWaitMillis = fakeWaitMillis;
        this.parameters = parameters;
        this.onErrorFunc = onErrorFunc;
        this.onSuccessFunc = onSuccessFunc;
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
            progress.setIndeterminate(true);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setMessage(progressMessage);
            progress.show();
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
                conn.setRequestMethod(method);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder();

                if (parameters != null){
                    for (Pair<String, String> param : parameters) {
                        builder.appendQueryParameter(param.first, param.second);
                    }
                }

                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
                error = false;

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