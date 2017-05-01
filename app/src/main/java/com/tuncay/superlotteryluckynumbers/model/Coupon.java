package com.tuncay.superlotteryluckynumbers.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mac on 1.05.2017.
 */

public class Coupon extends RealmObject{
    @PrimaryKey
    public String CouponId;
    public String User;
    public String GameType;
    public String Numbers;
    public String PlayTime;
    public String LotteryTime;
    public String ToRemind;
    public String ServerCalled;
    public int WinCount;


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
