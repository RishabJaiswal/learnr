package com.jombay.learnr;

import android.app.Application;

import io.realm.Realm;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Rishab on 26-09-2017.
 */

public class MyRealmApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        Realm.init(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}
