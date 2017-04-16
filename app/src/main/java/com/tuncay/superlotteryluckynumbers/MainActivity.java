package com.tuncay.superlotteryluckynumbers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.tuncay.superlotteryluckynumbers.adapter.CustomMainListAdapter;
import com.tuncay.superlotteryluckynumbers.db.LotteryContract;
import com.tuncay.superlotteryluckynumbers.db.LotteryDbHelper;
import com.tuncay.superlotteryluckynumbers.model.MainListElement;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

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



    public void Sifirla(View view) {
        for (View v: views) {
            if (v instanceof EditText){
                ((EditText) v).setText("");
            }
        }
    }

    public void Kaydet(View view) {
        if (elements == null || elements.size() == 0)
            return;

        LotteryDbHelper dbHelper = new LotteryDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS.SSS");

        String date = sdf.format(new Date());
        String lotteryTime = s.getSelectedItem().toString();

        for (MainListElement li : elements) {
            if (li.getOyna()){
                insertToDb(db, li, date, lotteryTime);
            }
        }
    }

    private void insertToDb(SQLiteDatabase db, MainListElement li, String date, String lotteryTime) {


        ContentValues vals = new ContentValues();

        String[] nums = li.getNumString().split("-");

        String[] dateLotteryArr = lotteryTime.split("/");
        String dateLottery = dateLotteryArr[2] + "/" + dateLotteryArr[1] + "/" + dateLotteryArr[0];

        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_GAME_TYPE, "Sup");
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_PLAY_TIME, date);
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_NUMS, li.getNumString());
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_LOTTERY_TIME, dateLottery);
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_SERVER_CALLED, "N");
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_TO_REMIND, "Y");
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_USER, "tuncayalt@gmail.com");
        vals.put(LotteryContract.LotteryEntry.COLUMN_NAME_WIN_COUNT, 3);

        db.insert("LotteryNumbers", null, vals);
    }

    public void SayiSil(View view) {
        LinearLayout vwParentRow = (LinearLayout)view.getParent();
        int position=(Integer) view.getTag();
        MainListElement element = adapter.getItem(position);
        elements.remove(element);
        adapter.notifyDataSetChanged();
    }

    public void Doldur(View view) {
        Random random = new Random();
        //Sifirla(view);

        ArrayList<Integer> randomIntegers = getRandomIntegers(random, numOfFields);
        Collections.sort(randomIntegers);
        int i = 0;
        for (View v: views) {
            if (v instanceof EditText){

                fillWithLuckyNum(((EditText) v),randomIntegers.get(i));
                i++;
            }
        }

        ListeyeEkle(view);
    }

    public void ListeyeEkle(View view) {

        ArrayList<Integer> currentNums = getCurrentNums();
        if (currentNums.size() < numOfFields)
            return;

        String currentNumString = "";
        for(int i = 0; i < currentNums.size(); i++){
            currentNumString += leftPadding(currentNums.get(i));
            if (i < currentNums.size() - 1)
                currentNumString += "-";
        }

        MainListElement listElement = new MainListElement(currentNumString, false);

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
