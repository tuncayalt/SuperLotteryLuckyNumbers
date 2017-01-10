package com.tuncay.sansoyunlaritahminsonuc;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.tuncay.sansoyunlaritahminsonuc.db.LotteryContract;
import com.tuncay.sansoyunlaritahminsonuc.db.LotteryDbHelper;
import com.tuncay.sansoyunlaritahminsonuc.model.ListElement;

import java.text.DateFormat;
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
    ArrayList<ListElement> elements;
    ArrayAdapter<ListElement> adapter;
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


        /*SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("maxNumber", 48);
        editor.putInt("numOfFields", 6);
        editor.apply();*/

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
        elements = new ArrayList<ListElement>();
        adapter = new CustomListAdapter(this, elements);
        listView.setAdapter(adapter);

        array_spinner = new ArrayList<String>();
        spinnerAdapter = new ArrayAdapter<String>(this,
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
        LotteryDbHelper dbHelper = new LotteryDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
    }

    public void SayiSil(View view) {
        LinearLayout vwParentRow = (LinearLayout)view.getParent();
        int position=(Integer) view.getTag();
        ListElement element = adapter.getItem(position);
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
    }

    public void ListeyeEkle(View view) {

        ArrayList<Integer> currentNums = getCurrentNums();
        if (currentNums.size() < numOfFields)
            return;

        String currentNumString = "";
        for(int i = 0; i < currentNums.size(); i++){
            currentNumString += String.format("%1$2s", currentNums.get(i));
            if (i < currentNums.size() - 1)
                currentNumString += "-";
        }

        ListElement listElement = new ListElement(currentNumString, false);

        elements.add(listElement);
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

        edtNum.setText(Integer.toString(number));
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
