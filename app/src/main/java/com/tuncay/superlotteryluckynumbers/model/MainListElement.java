package com.tuncay.superlotteryluckynumbers.model;

/**
 * Created by mac on 2.01.2017.
 */
public class MainListElement {


    String nums;
    boolean oyna;

    public MainListElement(String nums, boolean oyna){
        this.nums = nums;
        this.oyna = oyna;
    }

    public boolean getOyna() {
        return oyna;
    }
    public void setOyna(boolean oyna) {
        this.oyna = oyna;
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
