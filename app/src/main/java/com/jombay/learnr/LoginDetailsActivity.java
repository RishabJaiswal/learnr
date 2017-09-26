package com.jombay.learnr;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jombay.learnr.model.UserModel;
import com.jombay.learnr.pojos.AccessToken;
import com.jombay.learnr.pojos.CurrentUserMeta;
import com.jombay.learnr.pojos.LessonData;
import com.jombay.learnr.pojos.User;
import com.jombay.learnr.presenter.UserPresenter;

import io.realm.Realm;
import io.realm.RealmResults;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/*Actvity to show login screen
or details when user has already logged in*/
public class LoginDetailsActivity extends AppCompatActivity implements View.OnClickListener,
        BaseView, PopupMenu.OnMenuItemClickListener
{
    private ConnectivityManager connectivityManager;
    private FrameLayout mainContainer;
    private UserPresenter presenter;

    //views
    private View singinProgressBar, filterLessonsView, signinButton;
    EditText emailView, passwordView;
    private SharedPreferences sharedPreferences;
    private PopupMenu popupMenu;

    //adapters
    private LessonsAdapter lessonsAdapter;

    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //checking internet connectivity
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected())
        {
            setContentView(R.layout.fallback_no_internet);
            findViewById(R.id.retry_button).setOnClickListener(this);
        }
        else
        {
            decideLayout();
        }
    }

    //activity lifecycle
    @Override
    protected void onDestroy()
    {
        getLifecycle().removeObserver(presenter);
        super.onDestroy();
    }

    //view click listener callback
    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            /*checking internet again & setting corresponding layout*/
            case R.id.retry_button:
            {
                retry(view);
                break;
            }
            //user signs in
            case R.id.signin_button:
            {
                signinButton.setVisibility(View.INVISIBLE);
                singinProgressBar.setVisibility(View.VISIBLE);
                presenter.login(null, emailView.getText().toString(), passwordView.getText().toString());
                break;
            }
            //showing lessons filter option
            case R.id.filter_lessons:
            {
                popupMenu.show();
                break;
            }
        }
    }

    /*lessons filter popup menu
    click listener callback*/
    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        if (lessonsAdapter == null)
            return false;
        switch (item.getItemId())
        {
            case R.id.all:
            {
                lessonsAdapter.setAllData();
                return true;
            }
            case R.id.completed:
            {
                query(true);
                return true;

            }
            case R.id.pending:
            {
                query(false);
                return true;
            }
        }
        return false;
    }

    /*method to set MVP presenter
    presenter should be lifecycle aware*/
    @Override
    public void setPresenter()
    {
        presenter = new UserPresenter(this, this, ViewModelProviders.of(this).get(UserModel.class));
        getLifecycle().addObserver(presenter);
    }

    /*retry getting online, disable
    retry button & check connection again*/
    private void retry(View retryButton)
    {
        retryButton.setEnabled(false);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            decideLayout();
        }
        else
            retryButton.setEnabled(true);
    }

    /*decides which layout to user
    based on user's stored UID*/
    private void decideLayout()
    {
        setContentView(R.layout.activity_login_details);
        mainContainer = findViewById(R.id.loginDetailsContainer);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setPresenter();

        //set layout
        if (sharedPreferences.getString(getString(R.string.pref_uid), null) == null)
            setLoginLayout();
        else
            setDetailsLayout(true);
    }

    /*change to main layout and check if user has
    already logged in and add child layout accordingly*/
    private void setLoginLayout()
    {
        connectivityManager = null;
        //when no user already logged in
        if (sharedPreferences.getString(getString(R.string.pref_uid), null) == null)
        {
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f,
                    getResources().getDisplayMetrics());
            View loginView = getLayoutInflater().inflate(R.layout.view_login, null);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            layoutParams.setMargins(margin, margin, margin, margin);
            loginView.setLayoutParams(layoutParams);
            mainContainer.addView(loginView);

            //setting up views in login layout
            singinProgressBar = findViewById(R.id.signin_progress_bar);
            emailView = findViewById(R.id.login_email);
            passwordView = findViewById(R.id.login_password);
            signinButton = findViewById(R.id.signin_button);
            signinButton.setOnClickListener(this);
        }
    }

    /*setting details layout when user
    logged in or was already logged in*/
    private void setDetailsLayout(boolean isRefreshingToken)
    {
        View detailsView = getLayoutInflater().inflate(R.layout.view_user_details, null);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        detailsView.setLayoutParams(layoutParams);
        mainContainer.removeAllViews();
        mainContainer.addView(detailsView);
        filterLessonsView = findViewById(R.id.filter_lessons);

        /*if access token has expired
        getting refresh token*/
        if (isRefreshingToken && isAccessTokenExpired())
        {
            presenter.login(sharedPreferences.getString(getString(R.string.pref_refresh_token), null),
                    null, null);
        }
        else
        {
            presenter.getUserProfile("bearer",
                    sharedPreferences.getString(getString(R.string.pref_access_token), ""),
                    sharedPreferences.getString(getString(R.string.pref_uid), ""),
                    sharedPreferences.getString(getString(R.string.pref_company_id), ""));
        }


    }

    //successful login
    /*save data to shared preferences. Called when user logs
    in for the first time or when token is refreshed*/
    public void loginSuccessful(boolean isRefreshingToken, AccessToken accessToken)
    {
        sharedPreferences.edit()
                .putString(getString(R.string.pref_access_token), accessToken.getAccess_token())
                .putString(getString(R.string.pref_refresh_token), accessToken.getRefresh_token())
                .putLong(getString(R.string.pref_expires_in), accessToken.getExpires_in())
                .putLong(getString(R.string.pref_created_at), accessToken.getCreated_at())
                .apply();
        //getting current user_id
        presenter.getCurrentUser(isRefreshingToken, "bearer", accessToken.getAccess_token());
    }

    //Failed to login
    public void loginFailed(boolean isRefreshingToken)
    {
        if (!isRefreshingToken)
        {
            singinProgressBar.setVisibility(View.INVISIBLE);
            signinButton.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
    }

    //setting up current user by getting userId and companyId
    public void setCurrentUser(boolean isRefreshingToken, CurrentUserMeta currentUserMeta)
    {
        if (currentUserMeta == null)
        {
            if (!isRefreshingToken)
                singinProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show();
        }
        else
        {
            sharedPreferences.edit()
                    .putString(getString(R.string.pref_uid), currentUserMeta.get_id())
                    .putString(getString(R.string.pref_company_id), currentUserMeta.getCompany_ids()[0])
                    .apply();

            //not an automatic log in
            if (!isRefreshingToken)
            {
                //singinProgressBar.setVisibility(View.INVISIBLE);
                setDetailsLayout(isRefreshingToken);
            }
            //automatic login when access token expires
            else
            {
                presenter.getUserProfile("bearer",
                        sharedPreferences.getString(getString(R.string.pref_access_token), ""),
                        sharedPreferences.getString(getString(R.string.pref_uid), ""),
                        sharedPreferences.getString(getString(R.string.pref_company_id), ""));
            }
        }
    }

    //checking expiry of access token
    private boolean isAccessTokenExpired()
    {
        long currentTime = System.currentTimeMillis() / 1000;
        long createdAt = sharedPreferences.getLong(getString(R.string.pref_created_at), 0);
        long expiresIn = sharedPreferences.getLong(getString(R.string.pref_expires_in), 0);
        if (currentTime - createdAt >= expiresIn)
            return true;
        return false;
    }

    //setting user data in the view
    public void setUserProfile(User user)
    {
        findViewById(R.id.details_progress_bar).setVisibility(View.INVISIBLE);
        if (user == null)
        {
            Toast.makeText(this, R.string.data_not_received, Toast.LENGTH_SHORT).show();
            return;
        }
        //setting name, mobile, email, usernamee
        ((TextView) findViewById(R.id.name)).setText(user.getName());
        ((TextView) findViewById(R.id.user_name)).setText(user.getUsername());
        ((TextView) findViewById(R.id.phone_number)).setText(user.getMobile());
        ((TextView) findViewById(R.id.email)).setText(user.getEmail());

        //setting  lessons
        if (lessonsAdapter == null)
        {
            lessonsAdapter = new LessonsAdapter(this, user.getUser_lessons());
            lessonsAdapter.setAllLessons(user.getUser_lessons());
        }
        RecyclerView lessonsRecycler = findViewById(R.id.lesson_recycler);
        lessonsRecycler.setLayoutManager(new LinearLayoutManager(this));
        lessonsRecycler.setAdapter(lessonsAdapter);

        //setting popupmenu
        if (filterLessonsView != null && popupMenu == null)
        {
            popupMenu = new PopupMenu(this, filterLessonsView);
            popupMenu.inflate(R.menu.filter_options);
            popupMenu.setOnMenuItemClickListener(this);
            filterLessonsView.setOnClickListener(this);
        }
    }

    //query lessons for pending and completed ones
    private void query(boolean showCompleted)
    {
        Realm realm = null;
        try
        {
            realm = Realm.getDefaultInstance();
            RealmResults<LessonData> lessons;
            if (showCompleted)
            {
                lessons = realm.where(LessonData.class)
                        .equalTo(getString(R.string.field_user_status),
                                getString(R.string.field_status_value_completed))
                        .findAll();
            }
            else
            {
                lessons = realm.where(LessonData.class)
                        .notEqualTo(getString(R.string.field_user_status),
                                getString(R.string.field_status_value_completed))
                        .findAll();
            }
            if (lessonsAdapter != null)
                lessonsAdapter.setData(lessons);
        } finally
        {
            /*if (realm != null)
                realm.close();*/
        }
    }
}
