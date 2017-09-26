package com.jombay.learnr;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jombay.learnr.pojos.LessonData;

import io.realm.OrderedRealmCollection;

/**
 * Created by Rishab on 26-09-2017.
 * Adapter for Lessons Recycler view
 */

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.LessonHolder>
{
    private OrderedRealmCollection filteredLessons, allLessons;
    private Context context;

    LessonsAdapter(Context context, OrderedRealmCollection<LessonData> lessons)
    {
        this.context = context;
        this.filteredLessons = lessons;
    }

    @Override
    public LessonHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.LayoutParams rlp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        View view = LayoutInflater.from(context).inflate(R.layout.view_lesson, null);
        view.setLayoutParams(rlp);
        return new LessonHolder(view);
    }

    @Override
    public void onBindViewHolder(LessonHolder lessonHolder, int position)
    {
        LessonData lessonData = (LessonData) filteredLessons.get(position);
        lessonHolder.title.setText(lessonData.getLesson().getTitle());
        lessonHolder.status.setText(lessonData.getStatus());
    }

    @Override
    public int getItemCount()
    {
        return filteredLessons.size();
    }

    //to show only filtered lessons
    void setData(OrderedRealmCollection<LessonData> lessons)
    {
        if (filteredLessons == lessons)
            return;
        this.filteredLessons = lessons;
        notifyDataSetChanged();
    }

    //to show all the lessons
    void setAllData()
    {
        this.filteredLessons = allLessons;
        notifyDataSetChanged();
    }

    void setAllLessons(OrderedRealmCollection allLessons)
    {
        this.allLessons = allLessons;
    }

    //ViewHolder for lessons
    static class LessonHolder extends RecyclerView.ViewHolder
    {
        TextView title, status;

        LessonHolder(View itemView)
        {
            super(itemView);
            title = itemView.findViewById(R.id.lesson_title);
            status = itemView.findViewById(R.id.lesson_status);
        }
    }
}
