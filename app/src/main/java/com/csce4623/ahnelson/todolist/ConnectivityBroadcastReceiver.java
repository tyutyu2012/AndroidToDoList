package com.csce4623.ahnelson.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.MenuItem;

/**
 * Created by tongyu on 10/6/17.
 */

public class ConnectivityBroadcastReceiver extends BroadcastReceiver {

    public MenuItem muConnect;

    // passing in the menu connection
    ConnectivityBroadcastReceiver(MenuItem muConnect) {
        this.muConnect = muConnect;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // set the menu to connect or disconnect depends on network
        if (isConnected(context)) {
            muConnect.setTitle("Connected");
        } else {
            muConnect.setTitle("Disconnected");
        }
    }

    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

}