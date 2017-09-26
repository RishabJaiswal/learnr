package com.jombay.learnr.model;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Rishab on 22-09-2017.
 */

//to maintain only client for Retrofit SDK in the whole app
public class ClientGenerator
{
    private static final String BASE_URL = "https://api.es-q.co";
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build();

    //creating API client
    public static<S> S createClient(Class<S> clientClass)
    {
        return retrofit.create(clientClass);
    }
}
