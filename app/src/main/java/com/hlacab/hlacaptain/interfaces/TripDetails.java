package com.hlacab.hlacaptain.interfaces;

import com.hlacab.hlacaptain.model.Data;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface TripDetails {
    @FormUrlEncoded
    @POST("TripDetailsUpdate")
    Call<Data> requestData(@Field("vehicleReferenceNumber") String vref, @Field("captainReferenceNumber") String captainReference, @Field("distanceInMeters") String distanceInMeters,
                           @Field("durationInSeconds") String durationInSeconds, @Field("customerRating") String customerRating,
                           @Field("customerWaitingTimeInSeconds") String customerWaitingTime, @Field("originCityNameInArabic") String originCityName,
                           @Field("destinationCityNameInArabic") String destinationCityName, @Field("originLatitude") String originLatitude,
                           @Field("originLongitude") String originLongitude, @Field("destinationLatitude") String destinationLatitude,
                           @Field("destinationLongitude") String destinationLongitude, @Field("pickupTimestamp") String pickupTimeStamp,
                           @Field("dropoffTimestamp") String dropOffTimestamp);

}
