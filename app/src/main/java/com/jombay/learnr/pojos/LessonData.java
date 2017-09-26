package com.jombay.learnr.pojos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Rishab on 26-09-2017.
 * represents a lesson & all its metadata
 * And model schema for each lesson
 */

public class LessonData extends RealmObject
{
    @PrimaryKey
    private String lesson_id;
    private String status;
    private Lesson lesson;

    public String getStatus()
    {
        return status;
    }

    public Lesson getLesson()
    {
        return lesson;
    }

}
