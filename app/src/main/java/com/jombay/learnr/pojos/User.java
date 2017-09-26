package com.jombay.learnr.pojos;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Rishab on 25-09-2017.
 */

public class User extends RealmObject
{
    private String _id, email, mobile, name, username;
    private RealmList<LessonData> user_lessons;

    public User()
    {
    }

    public String get_id()
    {
        return _id;
    }

    public String getEmail()
    {
        return email;
    }

    public String getMobile()
    {
        return mobile;
    }

    public String getName()
    {
        return name;
    }

    public String getUsername()
    {
        return username;
    }

    public RealmList<LessonData> getUser_lessons()
    {
        return user_lessons;
    }

    public void setUser_lessons(RealmList<LessonData> user_lessons)
    {
        this.user_lessons = user_lessons;
    }
}
