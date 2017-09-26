package com.jombay.learnr.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jombay.learnr.pojos.AccessToken;
import com.jombay.learnr.pojos.CurrentUserMeta;
import com.jombay.learnr.pojos.LessonData;
import com.jombay.learnr.pojos.User;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rishab on 22-09-2017.
 * This class has all the necessary
 * methods to access different sets of data.
 * Each method representing different data
 * pbserved by the presenter of the views
 */

public class UserModel extends ViewModel
{
    private LearnrApiClient learnrApiClient;
    private Gson gson;
    private MutableLiveData<AccessToken> accesTokenLD = new MutableLiveData<>();
    private MutableLiveData<CurrentUserMeta> currentUserLD = new MutableLiveData<>();
    private MutableLiveData<User> userLD = new MutableLiveData<>();

    public UserModel()
    {
        learnrApiClient = ClientGenerator.createClient(LearnrApiClient.class);
        gson = new Gson();
    }

    /*making network calls to the required
    API endpoint to fetch access token*/
    public LiveData<AccessToken> getAccessToken(String resfreshToken, String username, String password)
    {
        //request body
        HashMap<String, String> accessTokenRequestBody = new HashMap<>();
        if (resfreshToken == null)
        {
            accessTokenRequestBody.put("username", username);
            accessTokenRequestBody.put("password", password);
            accessTokenRequestBody.put("grant_type", "password");
        }
        else
        {
            accessTokenRequestBody.put("refresh_token", resfreshToken);
            accessTokenRequestBody.put("grant_type", "refresh_token");
        }
        accessTokenRequestBody.put("scope", "user");

        //creating request
        learnrApiClient.getAccessToken(accessTokenRequestBody)
                .enqueue(new Callback<AccessToken>()
                {
                    @Override
                    public void onResponse(Call<AccessToken> call, Response<AccessToken> response)
                    {
                        accesTokenLD.setValue(response.body());
                    }

                    @Override
                    public void onFailure(Call<AccessToken> call, Throwable t)
                    {
                        accesTokenLD.setValue(null);
                    }
                });
        return accesTokenLD;
    }

    //get current user when user logged in
    public LiveData<CurrentUserMeta> getCurrentUser(String type, String accessToken)
    {
        learnrApiClient.getCurrentUser(type + " " + accessToken)
                .enqueue(new Callback<ResponseBody>()
                {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                    {
                        try
                        {
                            JSONObject userJson = new JSONObject(response.body().string()).getJSONObject("user");
                            currentUserLD.setValue(gson.fromJson(userJson.toString(), CurrentUserMeta.class));
                        } catch (Exception e)
                        {
                            currentUserLD.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t)
                    {
                        currentUserLD.setValue(null);
                    }
                });
        return currentUserLD;
    }

    /*get user's profile, including completed
    lessons and general information*/
    public LiveData<User> getUserProfile(String type, String accessToken, String uid, String companyID)
    {
        learnrApiClient.getUserProfile(type + " " + accessToken, uid, companyID)
                .enqueue(new Callback<ResponseBody>()
                {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                    {
                        try
                        {
                            //getting user's basic info
                            JSONObject userInfoJson = new JSONObject(response.body().string()).getJSONObject("user_profile");
                            User user = gson.fromJson(userInfoJson.getJSONObject("user_document").toString(), User.class);

                            //getting user's lessons
                            Type listType = new TypeToken<RealmList<LessonData>>() {}.getType();
                            RealmList<LessonData> lessonList = gson.fromJson(userInfoJson.getJSONArray("user_lessons").toString(), listType);
                            user.setUser_lessons(lessonList);
                            saveLessonsToRealm(user);
                            userLD.setValue(user);
                        }
                        catch (Exception e)
                        {
                            userLD.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t)
                    {
                        userLD.setValue(null);
                    }
                });
        return userLD;
    }

    //saving user's lessons to the database
    private void saveLessonsToRealm(final User user)
    {
        try
        {
            final Realm realm = Realm.getDefaultInstance();
            realm.executeTransactionAsync(
                    new Realm.Transaction()
                    {
                        @Override
                        public void execute(Realm bgRealm)
                        {
                            bgRealm.insertOrUpdate(user.getUser_lessons());
                        }
                    },
                    new Realm.Transaction.OnSuccess()
                    {
                        @Override
                        public void onSuccess()
                        {
                            realm.close();
                        }
                    },
                    new Realm.Transaction.OnError()
                    {
                        @Override
                        public void onError(Throwable error)
                        {
                            realm.close();
                        }
                    });

        } finally
        {
            //realm.close();
        }
    }
}
