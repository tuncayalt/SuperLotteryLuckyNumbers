package com.tuncay.superlotteryluckynumbers;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.tuncay.superlotteryluckynumbers.adapter.CustomMainListAdapter;
import com.tuncay.superlotteryluckynumbers.model.Coupon;
import com.tuncay.superlotteryluckynumbers.model.MainListElement;
import com.tuncay.superlotteryluckynumbers.service.IServerService;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    EditText edtNum1, edtNum2, edtNum3, edtNum4, edtNum5, edtNum6;
    ListView listView;
    ArrayList<Integer> currentNums;
    ArrayList<View> views;
    ArrayList<MainListElement> elements;
    ArrayAdapter<MainListElement> adapter;
    int maxNumber = 54;
    int numOfFields = 6;
    Spinner s;
    ArrayList<String> array_spinner;
    ArrayAdapter<String> spinnerAdapter;
    int pickerViewNumber = 0;
    int pickerMinNumber = 0;
    int pickerMaxNumber = 54;
    int numberToChoose = 6;
    String urlBase = "https://superlotteryluckynumbersserver.eu-gb.mybluemix.net/api/";
    IServerService serverService;

    List<Coupon> couponList;
    List<Coupon> managedCouponList;
    String userId;
    Realm realm;
    ProgressDialog progress;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentNums = new ArrayList<Integer>();
        edtNum1 = (EditText) findViewById(R.id.edtNum1);
        edtNum2 = (EditText) findViewById(R.id.edtNum2);
        edtNum3 = (EditText) findViewById(R.id.edtNum3);
        edtNum4 = (EditText) findViewById(R.id.edtNum4);
        edtNum5 = (EditText) findViewById(R.id.edtNum5);
        edtNum6 = (EditText) findViewById(R.id.edtNum6);
        views = getViewsInLayout((LinearLayout) findViewById(R.id.linearLayout));

        for (View v : views) {
            if (v instanceof EditText) {
                ((EditText) v).setKeyListener(null);
            }
        }

        listView = (ListView) findViewById(R.id.listView);
        elements = new ArrayList<>();
        adapter = new CustomMainListAdapter(this, elements);
        listView.setAdapter(adapter);

        array_spinner = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, array_spinner);
        s = (Spinner) findViewById(R.id.spnDate);
        s.setAdapter(spinnerAdapter);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serverService = retrofit.create(IServerService.class);

        GetDates task = new GetDates();
        task.execute();
        MobileAds.initialize(this, "ca-app-pub-5819132225601729~6536327892");


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5819132225601729/8013061097");
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("B919CF34582CDFA602B3A23BBF6A5516")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
    }

    public void Sifirla() {
        for (View v : views) {
            if (v instanceof EditText) {
                ((EditText) v).setText("");
                v.setBackgroundResource(R.drawable.textview_round);
            }
        }
        pickerMinNumber = 0;
        pickerViewNumber = 0;
    }

    public void NumPicker(final View view) {
        Sifirla();
        final Dialog d = new Dialog(this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.numpicker);
        d.setCanceledOnTouchOutside(false);
        final Button b1 = (Button) d.findViewById(R.id.btnNumPickTamam);
        b1.setEnabled(false);
        Button b2 = (Button) d.findViewById(R.id.btnNumPickIptal);
        LinearLayout llNumPicker = (LinearLayout) d.findViewById(R.id.llNumpicker);
        LinearLayout currentLayout = new LinearLayout(this);
        numberToChoose = 6;
        for (int i = pickerMinNumber; i < pickerMaxNumber; i++){

            if (i % 6 == 0){
                LinearLayout ll = new LinearLayout(this);
                LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                ll.setLayoutParams(llParams);
                llNumPicker.addView(ll);
                currentLayout = ll;
            }

            Button b = new Button(this);
            b.setTag(false);
            b.setText(leftPadding(i + 1));
            b.setBackgroundColor(Color.parseColor("#2980B9"));
            b.setTextColor(Color.parseColor("#EEEEEE"));

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.weight = 1;
            b.setLayoutParams(btnParams);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean tag = (boolean)v.getTag();
                    if (tag){
                        v.setTag(false);
                        v.setBackgroundColor(Color.parseColor("#2980B9"));
                        numberToChoose++;
                        b1.setEnabled(false);
                    }
                    else{
                        if (numberToChoose > 0) {
                            v.setTag(true);
                            v.setBackgroundColor(Color.parseColor("#C0392B"));
                            numberToChoose--;
                            if (numberToChoose == 0){
                                b1.setEnabled(true);
                            }
                        }
                    }
                }
            });
            currentLayout.addView(b);
        }

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberToChoose != 0 )
                    return;

                ArrayList<View> layouts = getViewsInLayout((LinearLayout) d.findViewById(R.id.llNumpicker));
                ArrayList<View> buttons = new ArrayList<View>();

                ArrayList<String> selectedNums = new ArrayList<String>();
                for (View layout : layouts) {
                    buttons.clear();
                    buttons = getViewsInLayout((LinearLayout) layout);
                    for (View button : buttons) {
                        if (button instanceof Button && (boolean) button.getTag()) {
                            selectedNums.add(((Button) button).getText().toString());
                        }
                    }
                }
                Collections.sort(selectedNums);
                int currentNum = -1;
                for (String num:selectedNums) {
                    currentNum++;
                    ((EditText) views.get(currentNum)).setText(num);
                }
                ListeyeEkle();
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sifirla();

                d.dismiss();
            }
        });
        d.show();
    }

    public void Kaydet(View view) {
        if (elements == null || elements.size() == 0) {
            Toast.makeText(this, "Henüz bir kupon oluşturmadınız.", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences sharedPref = this.getSharedPreferences("firebaseUserToken", MODE_PRIVATE);
        userId = sharedPref.getString("userId", "");

        showProgress("Kuponları kaydediyor...");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                goToSaved(userId);
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS.SSS");

        String date = sdf.format(new Date());
        String lotteryTime = s.getSelectedItem().toString();
        String[] dateLotteryArr = lotteryTime.split("/");
        String dateLottery = dateLotteryArr[2] + "/" + dateLotteryArr[1] + "/" + dateLotteryArr[0];
        couponList = new ArrayList<>();

        //JSONArray couponJsonArr = new JSONArray();

        for (MainListElement li : elements) {
            if (li.getOyna()) {

                Coupon coupon = new Coupon();
                coupon.setCouponId(java.util.UUID.randomUUID().toString());
                coupon.setUser(userId);
                coupon.setGameType("Sup");
                coupon.setNumbers(li.getNumString());
                coupon.setPlayTime(date);
                coupon.setLotteryTime(dateLottery);
                coupon.setToRemind("T");
                coupon.setServerCalled("F");
                coupon.setWinCount(-1);
                coupon.setDeleted(false);
                couponList.add(coupon);
            }
        }
        realm.beginTransaction();
        managedCouponList = realm.copyToRealm(couponList);
        realm.commitTransaction();

        Call<Boolean> couponsCall = serverService.insertCoupon(couponList);
        couponsCall.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    for (Coupon coupon : managedCouponList) {
                        coupon.setServerCalled("T");
                    }
                    realm.commitTransaction();
                    realm.refresh();
                }
                progress.dismiss();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    goToSaved(userId);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                progress.dismiss();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    goToSaved(userId);
                }
            }
        });

    }

    private void showProgress(String message) {
        progress = new ProgressDialog(this);
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage(message);
        progress.show();
    }

    private void goToSaved(String userId) {
        Intent intent = new Intent(this, SavedActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    public void SayiSil(View view) {
        int position = (Integer) view.getTag();
        MainListElement element = adapter.getItem(position);
        elements.remove(element);
        adapter.notifyDataSetChanged();
    }

    public void Doldur(View view) {
        Random random = new Random(getRanSeed());
        ArrayList<Integer> randomIntegers = getRandomIntegers(random, numOfFields);
        Collections.sort(randomIntegers);
        int i = 0;
        for (View v : views) {
            if (v instanceof EditText) {

                fillWithLuckyNum(((EditText) v), randomIntegers.get(i));
                i++;
            }
        }
        ListeyeEkle();
    }

    public long getRanSeed() {
        SharedPreferences sp = getSharedPreferences("sevdigimKelime", MODE_PRIVATE);
        String kelime = sp.getString("kelime", "");
        return kelime.hashCode() ^ System.nanoTime();
    }

    public void ListeyeEkle() {

        ArrayList<Integer> currentNums = getCurrentNums();
        if (currentNums.size() < numOfFields)
            return;

        String currentNumString = "";
        for (int i = 0; i < currentNums.size(); i++) {
            currentNumString += leftPadding(currentNums.get(i));
            if (i < currentNums.size() - 1)
                currentNumString += "-";
        }

        MainListElement listElement = new MainListElement(currentNumString, true);

        elements.add(listElement);
        adapter.notifyDataSetChanged();

    }

    public void Sec(View view) {
        MainListElement le = elements.get((Integer) view.getTag());

        le.setOyna(((CheckBox) view).isChecked());
        adapter.notifyDataSetChanged();
    }

    public ArrayList<Integer> getRandomIntegers(Random random, int count) {
        int randomNum = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();

        for (int i = 0; i < count; i++) {
            do {
                randomNum = random.nextInt(maxNumber) + 1;
            } while (result.contains(randomNum));
            result.add(randomNum);
        }
        return result;
    }

    private void fillWithLuckyNum(EditText edtNum, int number) {
        edtNum.setText(leftPadding(number));
    }

    private String leftPadding(Integer number) {
        String unpadded = Integer.toString(number);
        return "00".substring(unpadded.length()) + unpadded;
    }


    public ArrayList<Integer> getCurrentNums() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (View v : views) {
            if (v instanceof EditText && isNumeric(((EditText) v).getText().toString())) {
                result.add(Integer.parseInt(((EditText) v).getText().toString()));
            }
        }
        return result;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public ArrayList<View> getViewsInLayout(LinearLayout ll) {
        ArrayList<View> result = new ArrayList<View>();
        final int childcount = ll.getChildCount();
        for (int i = 0; i < childcount; i++) {
            View v = ll.getChildAt(i);
            result.add(v);
        }
        return result;
    }


    class GetDates extends AsyncTask<Object, Void, ArrayList<Date>> {

        @Override
        protected ArrayList<Date> doInBackground(Object... objects) {
            ArrayList<Date> result = new ArrayList<>();

            Calendar date = Calendar.getInstance();
            while (date.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY)
                date.add(Calendar.DATE, 1);

            for (int i = 0; i < 5; i++) {
                result.add(date.getTime());
                date.add(Calendar.DATE, 7);
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Date> result) {
            Format df = new SimpleDateFormat("dd/MM/yyyy");

            for (int i = 0; i < result.size(); i++) {
                array_spinner.add(df.format(result.get(i)));
            }

            spinnerAdapter.notifyDataSetChanged();
        }
    }
}
