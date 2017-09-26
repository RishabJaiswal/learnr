package com.jombay.learnr.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.Nullable;

import com.jombay.learnr.BaseView;
import com.jombay.learnr.LoginDetailsActivity;
import com.jombay.learnr.model.UserModel;
import com.jombay.learnr.pojos.AccessToken;
import com.jombay.learnr.pojos.CurrentUserMeta;
import com.jombay.learnr.pojos.User;

/**
 * Created by Rishab on 25-09-2017.
 * Defines all the data related to the user
 * in a presentable format. It takes raw/semi-raw
 * data from the UserModel class and has methods
 * to make them presentable in UI. Also, It will
 * be having a reference to the User View.
 * <p>
 * ------------LIFECYCLE HANDLING----------------
 * <p>
 * Presenters should be lifecycle aware to avoid
 * memory leaks.
 */
public class UserPresenter extends BasePresenter<LoginDetailsActivity> implements LifecycleObserver
{
    private UserModel userModel;
    private LifecycleOwner lifecycleOwner;
    private Observer<AccessToken> accessTokenObserver;

    //passing the required view and model
    public UserPresenter(LifecycleOwner lifecycleOwner, BaseView loginDetailsView, UserModel userModel)
    {
        super(loginDetailsView);
        this.lifecycleOwner = lifecycleOwner;
        this.userModel = userModel;
    }

    //freeing up the resources when no view is attached to this presenter
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy()
    {
        view = null;
        userModel = null;
        lifecycleOwner = null;
    }

    //called after signin button is clicked
    public void login(final String refreshToken, String username, String password)
    {
        if (accessTokenObserver == null)
        {
            accessTokenObserver = new Observer<AccessToken>()
            {
                @Override
                public void onChanged(@Nullable AccessToken accessToken)
                {
                    boolean isRefreshingToken = false;
                    if (refreshToken != null)
                        isRefreshingToken = true;
                    if (accessToken != null && accessToken.getAccess_token() != null)
                        view.loginSuccessful(isRefreshingToken, accessToken);
                    else
                        view.loginFailed(isRefreshingToken);
                }
            };
        }
        //observing changes in the AccessToken Live Dataw
        userModel.getAccessToken(refreshToken, username, password).observe(lifecycleOwner, accessTokenObserver);
    }

    //getting current user's uid
    public void getCurrentUser(final boolean isRefreshingToken, String type, String accessToken)
    {
        final LiveData<CurrentUserMeta> currentUserLD = userModel.getCurrentUser(type, accessToken);
        currentUserLD.observe(lifecycleOwner, new Observer<CurrentUserMeta>()
        {
            @Override
            public void onChanged(@Nullable CurrentUserMeta currentUserMeta)
            {
                view.setCurrentUser(isRefreshingToken, currentUserMeta);
                currentUserLD.removeObserver(this);
            }
        });
    }

    //getting user profile and lessons
    public void getUserProfile(String type, String accessToken, String uid, String companyID)
    {
        final LiveData<User> userLD = userModel.getUserProfile(type, accessToken, uid, companyID);
        userLD.observe(lifecycleOwner, new Observer<User>()
        {
            @Override
            public void onChanged(@Nullable User user)
            {
                view.setUserProfile(user);
                userLD.removeObserver(this);
            }
        });
    }
}
