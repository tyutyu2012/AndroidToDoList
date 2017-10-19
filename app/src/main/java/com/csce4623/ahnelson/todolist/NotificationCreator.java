package com.csce4623.ahnelson.todolist;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Created by tongyu on 10/5/17.
 */

public class NotificationCreator extends BroadcastReceiver {
    private String title, content, done, date;
    private int id;

    @Override
    public void onReceive(Context context, Intent intent) {
        // get the information from the intent
        Bundle b = intent.getExtras();
        id = b.getInt("id");
        title = b.getString("title");
        content = b.getString("content");
        done = b.getString("done");
        date = b.getString("date");

        //NotificationId - arbitrary ID to associate with the notification
        int notificationId = id;
        //Create a Notification Builder object
        // set Icon to a given Icon
        // set the Title to "Hello World"
        // set the Content to "This is the content of the notification"
        // No ChannelID because not API 26 (Oreo)
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("Due date : " + date);

        // Create an Intent to start the activity for the MainActivity
        Intent alarmIntent = new Intent(context, TaskContent.class);
        // Pass all the intent when you click on the notification, but only id is really needed
        alarmIntent.putExtras(b);

        // Create a back stack builder object
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Create the parent stack (none in this case)
        stackBuilder.addParentStack(HomeActivity.class);
        // Add the MainActivity as the last Intent
        stackBuilder.addNextIntent(alarmIntent);
        // No other options to play with in the StackBuilder, but you can manipulate parts of the
        // backstack if necessary

        //Create a PendingIntent from the stackBuilder to create a new Task with the activity
        //to be launched at the top
        PendingIntent alarmPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        // Set the onClick() method for the Notification to the PendingIntent created
        mBuilder.setContentIntent(alarmPendingIntent);
        //Get an Instance of the system's NotificationManager object
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Build and show the notification
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    // getter and setters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDone() {
        return done;
    }

    public String getDate() {
        return date;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
