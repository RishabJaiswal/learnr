package com.jombay.learnr.model;

import com.jombay.learnr.pojos.AccessToken;
import com.jombay.learnr.pojos.User;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by Rishab on 22-09-2017.
 * API client for learnr API where
 * each method is a request to different
 * API endpoints
 */

public interface LearnrApiClient
{
    //getting the access token
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/oauth/token.json")
    Call<AccessToken> getAccessToken(@QueryMap Map<String, String> queries);

    //getting current user
    @Headers("Content-Type: application/json")
    @GET("/users/current.json")
    Call<ResponseBody> getCurrentUser(@Header("Authorization") String type_AccessToken);

    //getting current user
    @GET("/companies/{cid}/sq/users/{uid}/user_profile?include[user_lessons][only][]=status&include[user_lessons][include][lesson][only]=title&include[user_lessons][only][]=lesson_id&select[]=_id&select[]=user_document")
    Call<ResponseBody> getUserProfile(@Header("Authorization") String type_AccessToken,
                              @Path("uid") String userID,
                              @Path("cid") String companyID);
}
