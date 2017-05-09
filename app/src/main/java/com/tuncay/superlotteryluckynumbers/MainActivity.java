package com.tuncay.superlotteryluckynumbers;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
    String urlBase = "https://superlotteryluckynumbersserver.eu-gb.mybluemix.net/api/";
    IServerService serverService;

    List<Coupon> couponList;
    Realm realm;


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

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlBase)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serverService = retrofit.create(IServerService.class);

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
        if (elements == null || elements.size() == 0) {
            Toast.makeText(this, "Henüz bir kupon oluşturmadınız.", Toast.LENGTH_LONG).show();
            return;
        }
        final String userName = getIntent().getStringExtra("userMail");
        if (userName == null || userName.isEmpty()){
            Toast.makeText(this, "Kuponlarınızı kaydedebilmek için ana sayfadan giriş yapın.", Toast.LENGTH_LONG).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS.SSS");

        String date = sdf.format(new Date());
        String lotteryTime = s.getSelectedItem().toString();
        String[] dateLotteryArr = lotteryTime.split("/");
        String dateLottery = dateLotteryArr[2] + "/" + dateLotteryArr[1] + "/" + dateLotteryArr[0];
        couponList = new ArrayList<>();

        //JSONArray couponJsonArr = new JSONArray();

        for (MainListElement li : elements) {
            if (li.getOyna()){

                Coupon coupon = new Coupon();
                coupon.setCouponId(java.util.UUID.randomUUID().toString());
                coupon.setUser(userName);
                coupon.setGameType("Sup");
                coupon.setNumbers(li.getNumString());
                coupon.setPlayTime(date);
                coupon.setLotteryTime(dateLottery);
                coupon.setToRemind("T");
                coupon.setServerCalled(false);
                coupon.setWinCount(0);
                coupon.setDeleted(false);
                couponList.add(coupon);

                /*JSONObject couponJson = new JSONObject();
                try {
                    couponJson.put("CouponId", coupon.getCouponId());
                    couponJson.put("User", coupon.getUser());
                    couponJson.put("GameType", coupon.getGameType());
                    couponJson.put("Numbers", coupon.getNumbers());
                    couponJson.put("PlayTime", coupon.getPlayTime());
                    couponJson.put("LotteryTime", coupon.getLotteryTime());
                    couponJson.put("ToRemind", coupon.getToRemind());
                    couponJson.put("ServerCalled", coupon.isServerCalled());
                    couponJson.put("WinCount", coupon.getWinCount());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                couponJsonArr.put(couponJson);*/
            }
        }
        realm.beginTransaction();
        realm.copyToRealm(couponList);
        realm.commitTransaction();

        Call<Boolean> couponsCall = serverService.insertCoupon(couponList);
        couponsCall.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()){
                    realm.beginTransaction();
                    for (Coupon coupon: couponList) {
                        coupon.setServerCalled(true);
                    }
                    realm.commitTransaction();
                }
                goToSaved(userName);
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                goToSaved(userName);
            }
        });

    }

    private void goToSaved(String userName) {
        Intent intent = new Intent(this, SavedActivity.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
        finish();
    }

    public void SayiSil(View view) {
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
        return kelime.hashCode() ^ System.nanoTime();
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
