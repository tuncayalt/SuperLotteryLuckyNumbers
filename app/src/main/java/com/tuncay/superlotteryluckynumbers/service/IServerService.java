package com.tuncay.superlotteryluckynumbers.service;

import com.tuncay.superlotteryluckynumbers.model.Cekilis;
import com.tuncay.superlotteryluckynumbers.model.Coupon;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by mac on 3.05.2017.
 */

public interface IServerService {
    @GET("cekilis")
    Call<Cekilis> getSonCekilis();

    @GET("coupon/{user}")
    Call<List<Coupon>> getCoupons(@Path("user") String user);

    @DELETE("coupon/{couponId}")
    Call<Boolean> deleteCoupon(@Path("couponId") String couponId);

    @POST("coupon")
    Call<Boolean> insertCoupon(@Body List<Coupon> couponList);
}
