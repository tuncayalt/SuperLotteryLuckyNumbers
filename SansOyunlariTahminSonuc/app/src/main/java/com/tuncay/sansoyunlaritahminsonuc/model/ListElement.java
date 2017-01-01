package com.tuncay.sansoyunlaritahminsonuc.model;

import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by mac on 2.01.2017.
 */
public class ListElement {


    String nums;
    boolean oyna;

    public ListElement(String nums, boolean oyna){
        this.nums = nums;
        this.oyna = oyna;
    }

    public boolean getOyna() {
        return oyna;
    }

    public String getNums() {
        return nums;
    }
}
