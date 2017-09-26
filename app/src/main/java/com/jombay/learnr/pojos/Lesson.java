package com.jombay.learnr.pojos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Rishab on 26-09-2017.
 */

public class Lesson extends RealmObject
{
    @PrimaryKey
    private String title;

    public String getTitle()
    {
        return title;
    }
}
