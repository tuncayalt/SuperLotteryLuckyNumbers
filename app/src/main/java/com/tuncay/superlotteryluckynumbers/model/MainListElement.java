package com.tuncay.superlotteryluckynumbers.model;

/**
 * Created by mac on 2.01.2017.
 */
public class MainListElement {

    String nums;

    public MainListElement(String nums){
        this.nums = nums;
    }

    public String getNumString() {
        return nums;
    }

    public String[] getNumsStringArray(){
        return nums.split("-");
    }

    public int[] getNumsIntArray(){
        String[] stringArr = getNumsStringArray();
        int[] result = new int[stringArr.length];
        for (int i = 0; i < stringArr.length; i++) {
            result[i] = Integer.parseInt(stringArr[i]);
        }
        return result;
    }
}
