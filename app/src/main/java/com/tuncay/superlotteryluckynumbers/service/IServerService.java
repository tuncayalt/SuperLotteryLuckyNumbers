package com.tuncay.superlotteryluckynumbers.service;

import com.tuncay.superlotteryluckynumbers.model.Cekilis;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by mac on 3.05.2017.
 */

public interface IServerService {
    @GET("cekilis")
    Call<Cekilis> getSonCekilis();
}
