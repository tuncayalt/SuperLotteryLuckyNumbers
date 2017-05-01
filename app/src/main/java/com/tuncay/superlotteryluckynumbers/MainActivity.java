package com.tuncay.superlotteryluckynumbers;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import com.tuncay.superlotteryluckynumbers.adapter.CustomMainListAdapter;
import com.tuncay.superlotteryluckynumbers.db.LotteryContract;
import com.tuncay.superlotteryluckynumbers.db.LotteryDbHelper;
import com.tuncay.superlotteryluckynumbers.model.Coupon;
import com.tuncay.superlotteryluckynumbers.model.MainListElement;
import com.tuncay.superlotteryluckynumbers.service.MyHttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener{

    EditText edtNum1, edtNum2,edtNum3,edtNum4,edtNum5,edtNum6;
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
    String urlInsertCoupons = "https://superlotteryluckynumbersserver.eu-gb.mybluemix.net/api/coupon";
    List<Coupon> couponList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentNums = new ArrayList<Integer>();
        edtNum1 = (EditText)findViewById(R.id.edtNum1);
        edtNum2 = (EditText)findViewById(R.id.edtNum2);
        edtNum3 = (EditText)findViewById(R.id.edtNum3);
        edtNum4 = (EditText)findViewById(R.id.edtNum4);
        edtNum5 = (EditText)findViewById(R.id.edtNum5);
        edtNum6 = (EditText)findViewById(R.id.edtNum6);
        views = getViewsInLayout((LinearLayout) findViewById(R.id.linearLayout));

        for (View v: views) {
            if (v instanceof EditText){
                ((EditText) v).setKeyListener(null);
            }
        }

        listView = (ListView) findViewById(R.id.listView);
        elements = new ArrayList<>();
        adapter = new CustomMainListAdapter(this, elements);
        listView.setAdapter(adapter);

        array_spinner = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, array_spinner);
        s = (Spinner) findViewById(R.id.spnDate);
        s.setAdapter(spinnerAdapter);

        GetDates task = new GetDates();
        task.execute();

    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
    }

    public void Sifirla() {
        for (View v: views) {
            if (v instanceof EditText){
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
        Button b1 = (Button) d.findViewById(R.id.btnNumPickTamam);
        Button b2 = (Button) d.findViewById(R.id.btnNumPickIptal);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.npNumPiker);
        np.setMaxValue(pickerMaxNumber);
        np.setMinValue(pickerMinNumber + 1);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        (views.get(0)).setBackgroundResource(R.drawable.textview_selected);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                ((EditText)views.get(pickerViewNumber)).setText(leftPadding(np.getValue()));
                views.get(pickerViewNumber).setBackgroundResource(R.drawable.textview_round);
                pickerMinNumber = np.getValue();
                if (pickerMinNumber >= pickerMaxNumber && pickerViewNumber < 5){
                    Sifirla();
                    np.setMinValue(1);
                    np.setValue(1);
                    views.get(pickerViewNumber).setBackgroundResource(R.drawable.textview_selected);
                    return;
                }
                np.setMinValue(pickerMinNumber + 1);
                pickerViewNumber++;
                if (pickerViewNumber >= 6){
                    pickerMinNumber = 0;
                    pickerViewNumber = 0;
                    ListeyeEkle();
                    d.dismiss();
                }
                else{
                    views.get(pickerViewNumber).setBackgroundResource(R.drawable.textview_selected);
                }
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Sifirla();

                d.dismiss();
            }
        });
        d.show();
    }

    public void Kaydet(View view) {
        if (elements == null || elements.size() == 0)
            return;

        LotteryDbHelper dbHelper = new LotteryDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS.SSS");

        String date = sdf.format(new Date());
        String lotteryTime = s.getSelectedItem().toString();

        couponList = new ArrayList<>();
        JSONArray couponJsonArr = new JSONArray();
        for (MainListElement li : elements) {
            if (li.getOyna()){
                final Coupon coupon = new Coupon();
                coupon.CouponId = java.util.UUID.randomUUID().toString();
                coupon.User = getIntent().getStringExtra("userMail");
                coupon.GameType = "Sup";
                coupon.Numbers = li.getNumString();
                coupon.PlayTime = date;
                coupon.LotteryTime = lotteryTime;
                coupon.ToRemind = "T";
                coupon.ServerCalled = "F";
                coupon.WinCount = 0;
                couponList.add(coupon);

                JSONObject couponJson = new JSONObject();
                try {
                    couponJson.put("CouponId", coupon.CouponId);
                    couponJson.put("User", coupon.User);
                    couponJson.put("GameType", coupon.GameType);
                    couponJson.put("Numbers", coupon.Numbers);
                    couponJson.put("PlayTime", coupon.PlayTime);
                    couponJson.put("LotteryTime", coupon.LotteryTime);
                    couponJson.put("ToRemind", coupon.ToRemind);
                    couponJson.put("WinCount", coupon.WinCount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                couponJsonArr.put(couponJson);

                insertToDb(db, coupon);
            }
        }
        MyHttpHandler httpHandler = new MyHttpHandler(this, "", urlInsertCoupons, "POST", 0, new Callable() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        }, new Callable() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        }, couponJsonArr.toString());
        httpHandler.execute();


        Intent intent = new Intent(this, SavedActivity.class);
        startActivity(intent);
        finish();
    }

    private void insertToDb(SQLiteDatabase db, Coupon coupon) {
        ContentValues vals = new ContentValues();

        String[] dateLotteryArr = coupon.LotteryTime.split("/");
        String dateLottery = dateLotteryArr[2] + "/" + dateLotteryArr[1] + "/" + dateLotteryArr[0];

        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_ID, coupon.CouponId);
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_GAME_TYPE, coupon.GameType);
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_PLAY_TIME, coupon.PlayTime);
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_NUMS, coupon.Numbers);
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_LOTTERY_TIME, dateLottery);
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_SERVER_CALLED, coupon.ServerCalled);
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_TO_REMIND, coupon.ToRemind);
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_USER, coupon.User);
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_WIN_COUNT, coupon.WinCount);

        db.insert("Coupons", null, vals);
    }

    public void SayiSil(View view) {
        LinearLayout vwParentRow = (LinearLayout)view.getParent();
        int position=(Integer) view.getTag();
        MainListElement element = adapter.getItem(position);
        elements.remove(element);
        adapter.notifyDataSetChanged();
    }

    public void Doldur(View view) {
        Random random = new Random(getRanSeed());
        ArrayList<Integer> randomIntegers = getRandomIntegers(random, numOfFields);
        Collections.sort(randomIntegers);
        int i = 0;
        for (View v: views) {
            if (v instanceof EditText){

                fillWithLuckyNum(((EditText) v),randomIntegers.get(i));
                i++;
            }
        }
        ListeyeEkle();
    }

    public long getRanSeed(){
        SharedPreferences sp = getSharedPreferences("sevdigimKelime", MODE_PRIVATE);
        String kelime = sp.getString("kelime", "");
        long seed = kelime.hashCode() ^ System.nanoTime();
        return seed;
    }

    public void ListeyeEkle() {

        ArrayList<Integer> currentNums = getCurrentNums();
        if (currentNums.size() < numOfFields)
            return;

        String currentNumString = "";
        for(int i = 0; i < currentNums.size(); i++){
            currentNumString += leftPadding(currentNums.get(i));
            if (i < currentNums.size() - 1)
                currentNumString += "-";
        }

        MainListElement listElement = new MainListElement(currentNumString, true);

        elements.add(listElement);
        adapter.notifyDataSetChanged();

    }

    public void Sec(View view){
        MainListElement le = elements.get((Integer)view.getTag());

        le.setOyna(((CheckBox)view).isChecked());
        adapter.notifyDataSetChanged();
    }

    public ArrayList<Integer> getRandomIntegers(Random random, int count){
        int randomNum = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();

        for (int i = 0; i < count; i++){
            do {
                randomNum = random.nextInt(maxNumber) + 1;
            }while (result.contains(randomNum));
            result.add(randomNum);
        }
        return result;
    }

    private void fillWithLuckyNum(EditText edtNum, int number) {
        edtNum.setText(leftPadding(number));
    }

    private String leftPadding(Integer number){
        String unpadded = Integer.toString(number);
        return "00".substring(unpadded.length()) + unpadded;
    }


    public ArrayList<Integer> getCurrentNums(){
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (View v : views) {
            if (v instanceof EditText && isNumeric(((EditText) v).getText().toString())){
                result.add(Integer.parseInt(((EditText) v).getText().toString()));
            }
        }
        return result;
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public ArrayList<View> getViewsInLayout(LinearLayout ll){
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
            while( date.get( Calendar.DAY_OF_WEEK ) != Calendar.THURSDAY )
                date.add( Calendar.DATE, 1 );

            for (int i = 0; i < 5; i++){
                result.add(date.getTime());
                date.add( Calendar.DATE, 7 );
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
