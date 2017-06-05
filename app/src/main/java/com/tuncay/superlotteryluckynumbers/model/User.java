package com.tuncay.superlotteryluckynumbers.model;

/**
 * Created by mac on 15.05.2017.
 */

public class User {
    private String prev_token;
    private String recent_token;
    private String user_id;
    private String push_cekilis;
    private String push_win;

    public User(){

    }

    public String getPrev_token() {
        return prev_token;
    }

    public void setPrev_token(String prev_token) {
        this.prev_token = prev_token;
    }

    public String getRecent_token() {
        return recent_token;
    }

    public void setRecent_token(String recent_token) {
        this.recent_token = recent_token;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPush_cekilis() {
        return push_cekilis;
    }

    public void setPush_cekilis(String push_cekilis) {
        this.push_cekilis = push_cekilis;
    }

    public String getPush_win() {
        return push_win;
    }

    public void setPush_win(String push_win) {
        this.push_win = push_win;
    }


}
