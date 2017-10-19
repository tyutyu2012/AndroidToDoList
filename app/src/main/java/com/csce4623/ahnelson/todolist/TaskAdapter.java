package com.csce4623.ahnelson.todolist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tongyu on 10/3/17.
 */
//https://stackoverflow.com/questions/25424045/how-to-store-class-into-android-listview
public class TaskAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private List<Task> tasks;

    public TaskAdapter(Context context, List<Task> tasks)
    {
        this.inflater = LayoutInflater.from(context);
        this.tasks = tasks;
    }

    @Override
    public int getCount() {
        return this.tasks.size();
    }

    @Override
    public Task getItem(int position) {
        return this.tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.tasks.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Task item = getItem(position);

        if(convertView == null) {
            // If convertView is null we have to inflate a new layout
            convertView = this.inflater.inflate(R.layout.activity_one_task, parent, false);
            final TaskView taskView = new TaskView();
            taskView.tvId = (TextView) convertView.findViewById(R.id.tv_id);
            taskView.tvTitle = (TextView) convertView.findViewById(R.id.task_title);
            taskView.ls_done = (TextView) convertView.findViewById(R.id.ls_done);

            // We set the view holder as tag of the convertView so we can access the view holder later on.
            convertView.setTag(taskView);
        }

        // Retrieve the view holder from the convertView
        final TaskView taskView = (TaskView) convertView.getTag();

        // Bind the values to the views
        taskView.tvId.setText(item.getId());
        taskView.tvTitle.setText(item.getTitle());
        // For the check mark,if the task is done, display check marks, else display 'X'
        if(item.getDone().equals("true"))
            taskView.ls_done.setText("\u2713");
        else
            taskView.ls_done.setText("X");

        return convertView;
    }
}
