package com.hlacab.hlacaptain.interfaces;

import com.hlacab.hlacaptain.model.Data;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by gopinath on 18/02/18.
 */

public interface CaptainDetails {

    @FormUrlEncoded
    @POST("LocUpdate")
    Call<Data> insertData(@Field("vref") String vref, @Field("clat") String clat, @Field("clong") String clong, @Field("cust") String cust);
}
