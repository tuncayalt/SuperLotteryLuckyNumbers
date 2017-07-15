package com.tuncay.superlotteryluckynumbers.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mac on 1.05.2017.
 */

public class Coupon extends RealmObject{
    @PrimaryKey
    private String couponId;
    private String user;
    private String gameType;
    private String numbers;
    private String playTime;
    private String lotteryTime;
    private String toRemind;
    private String serverCalled;
    private int winCount;
    private boolean isDeleted;

    public Coupon(){

    }

    public String getServerCalled() {
        return serverCalled;
    }

    public void setServerCalled(String serverCalled) {
        this.serverCalled = serverCalled;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getNumbers() {
        return numbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    public String getLotteryTime() {
        return lotteryTime;
    }

    public void setLotteryTime(String lotteryTime) {
        this.lotteryTime = lotteryTime;
    }

    public String getToRemind() {
        return toRemind;
    }

    public void setToRemind(String toRemind) {
        this.toRemind = toRemind;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

}
