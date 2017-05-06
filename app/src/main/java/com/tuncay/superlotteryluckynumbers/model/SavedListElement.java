package com.tuncay.superlotteryluckynumbers.model;

/**
 * Created by mac on 3.04.2017.
 */
public class SavedListElement {

    String lotteryDate;
    String nums;
    int winCount;
    String couponId;

    public String getNums() {
        return nums;
    }

    public void setNums(String nums) {
        this.nums = nums;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public String getLotteryDate() {
        return lotteryDate;
    }

    public void setLotteryDate(String lotteryDate) {
        this.lotteryDate = lotteryDate;
    }

    public SavedListElement(String lotteryDate, String nums, int winCount, String couponId){
        this.lotteryDate = lotteryDate;
        this.nums = nums;
        this.winCount = winCount;
        this.couponId = couponId;
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
