package com.tuncay.superlotteryluckynumbers.model;

import io.realm.RealmObject;

/**
 * Created by mac on 1.05.2017.
 */

public class Coupon extends RealmObject{
    private String CouponId;
    private String User;
    private String GameType;
    private String Numbers;
    private String PlayTime;
    private String LotteryTime;
    private String ToRemind;
    private String ServerCalled;
    private int WinCount;

    public Coupon(){

    }

    public String getCouponId() {
        return CouponId;
    }

    public void setCouponId(String couponId) {
        CouponId = couponId;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public String getGameType() {
        return GameType;
    }

    public void setGameType(String gameType) {
        GameType = gameType;
    }

    public String getNumbers() {
        return Numbers;
    }

    public void setNumbers(String numbers) {
        Numbers = numbers;
    }

    public String getPlayTime() {
        return PlayTime;
    }

    public void setPlayTime(String playTime) {
        PlayTime = playTime;
    }

    public String getLotteryTime() {
        return LotteryTime;
    }

    public void setLotteryTime(String lotteryTime) {
        LotteryTime = lotteryTime;
    }

    public String getToRemind() {
        return ToRemind;
    }

    public void setToRemind(String toRemind) {
        ToRemind = toRemind;
    }

    public String getServerCalled() {
        return ServerCalled;
    }

    public void setServerCalled(String serverCalled) {
        ServerCalled = serverCalled;
    }

    public int getWinCount() {
        return WinCount;
    }

    public void setWinCount(int winCount) {
        WinCount = winCount;
    }


}
