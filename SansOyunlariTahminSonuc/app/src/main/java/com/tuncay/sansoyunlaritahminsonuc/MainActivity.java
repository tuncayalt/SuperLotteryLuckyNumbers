package com.tuncay.sansoyunlaritahminsonuc;

import android.app.LauncherActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tuncay.sansoyunlaritahminsonuc.model.ListElement;

import java.util.ArrayList;
import java.util.Collections;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
    }


    public void Sifirla(View view) {
        for (View v: views) {
            if (v instanceof EditText){
                ((EditText) v).setText("");
            }
        }
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
}
