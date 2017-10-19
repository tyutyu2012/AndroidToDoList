package com.csce4623.ahnelson.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by tongyu on 10/2/17.
 */

public class TaskContent extends AppCompatActivity {
    public static final String LOCAL_FILE_NAME = "mytodofile.local";
    private ConnectivityBroadcastReceiver myConnBR;
    private MenuItem muConnect;
    private EditText content, edTitle, date;
    private CheckBox done;
    private int ID = -1;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        // initialize the component first
        initializeComponents();
        // checking if there is exists bundle
        checkingBundle();

        // splitDate
        splitDate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        muConnect = menu.findItem(R.id.Connection);
        connectionToBroadcastReceiver();
        return super.onCreateOptionsMenu(menu);
    }

    private void initializeComponents() {
        edTitle = (EditText) findViewById(R.id.edTitle);
        content = (EditText) findViewById(R.id.etNoteContent);
        done = (CheckBox) findViewById(R.id.cbDone);
        date = (EditText) findViewById(R.id.etDatePicker);

        // set the date into current date
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String dateString = sdf.format(time);
        date.setText(dateString + " 11:59");

        // for the back button
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void splitDate()
    {
        String[] dateTime = String.valueOf(date.getText()).split("\\s+");
        String[] monthDayYear = dateTime[0].split("/");
        String[] hourMinute = dateTime[1].split(":");

        mMonth = Integer.valueOf(monthDayYear[0]);
        mDay = Integer.valueOf(monthDayYear[1]);
        mYear= Integer.valueOf(monthDayYear[2]);
        mHour = Integer.valueOf(hourMinute[0]);
        mMinute = Integer.valueOf(hourMinute[1]);
    }

    public void checkingBundle() {
        // get the intern from the parent class
        Bundle b = getIntent().getExtras();

        // if call from click the title, get the information
        if (b != null) {
            ID = b.getInt("id");

            // get specific data row using ID
            Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI, ToDoProvider.projection, ToDoProvider.TODO_TABLE_COL_ID + "=" + String.valueOf(ID), null, null);

            if (myCursor.getCount() == 0) {
                Toast.makeText(getApplicationContext(), "Note Not Exist", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            } else {
                myCursor.moveToNext();
                int titleIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_TITLE);
                int contentIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_CONTENT);
                int doneIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_DONE);
                int dateIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_DATE);

                // getting the data using the indexs
                edTitle.setText(myCursor.getString(titleIndex));
                content.setText(myCursor.getString(contentIndex));
                if (myCursor.getString(doneIndex).equals("true"))
                    done.setChecked(true);
                date.setText(myCursor.getString(dateIndex));
            }
        }

        // set the cursor to the end
        edTitle.setSelection(edTitle.getText().length());
    }

    private void insertNewData() {
        //getting the current values
        String currentTitle = String.valueOf(edTitle.getText());
        String currentContent = String.valueOf(content.getText());
        String currentDone = String.valueOf(done.isChecked());
        String currentDate = String.valueOf(date.getText());
        if (myConnBR.isConnected(this)) {
            //Create a ContentValues object
            ContentValues myCV = new ContentValues();

            myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, currentTitle);
            myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, currentContent);
            myCV.put(ToDoProvider.TODO_TABLE_COL_DONE, currentDone);
            myCV.put(ToDoProvider.TODO_TABLE_COL_DATE, currentDate);

            //Perform the insert function using the ContentProvider
            getContentResolver().insert(ToDoProvider.CONTENT_URI, myCV);

            // get the id of the last added
            String[] projection = {
                    ToDoProvider.TODO_TABLE_COL_ID};
            //Perform a query to get all rows in the DB
            Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, null);

            // move to the last info added.
            myCursor.moveToLast();
            int idIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_ID);
            // getting the data using the indexs
            ID = Integer.valueOf(myCursor.getString(idIndex));

            // sent a notification
            alarmNotification(String.valueOf(ID), currentTitle, currentContent, currentDone, currentDate);
            Toast.makeText(getApplicationContext(), "New ToDo Added", Toast.LENGTH_LONG).show();

            // move back main activity
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            // 0 for datas that need to save, 1 for new datas
            saveNoteToFile("1");
        }
    }

    public void onClickSave(View v) {
        // for the title, you can only edit at the when you first create TODOLIST
        if (ID != -1) {
            if (myConnBR.isConnected(this)) {
                String currentTitle = String.valueOf(edTitle.getText());

                String currentContent = String.valueOf(content.getText());
                String currentDone = String.valueOf(done.isChecked());
                String currentDate = String.valueOf(date.getText());

                ContentValues myCV = new ContentValues();
                //Put key_value pairs based on the column names, and the values
                myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, currentTitle);
                myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, currentContent);
                myCV.put(ToDoProvider.TODO_TABLE_COL_DONE, currentDone);
                myCV.put(ToDoProvider.TODO_TABLE_COL_DATE, currentDate);

                // update the value to the database
                getContentResolver().update(ToDoProvider.CONTENT_URI, myCV, ToDoProvider.TODO_TABLE_COL_ID + "=" + ID, null);

                alarmNotification(String.valueOf(ID), currentTitle,currentContent,currentDone,currentDate);

                // if you press the save, it will take you back to the main listview
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
            } else {
                // 0 for update on file
                saveNoteToFile("0");
            }
        } else {
            insertNewData();
        }

    }

    public void alarmNotification(String id, String title, String content, String done, String date) {
        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, NotificationCreator.class);
        Bundle b = new Bundle();
        b.putInt("id", Integer.valueOf(id));
        b.putString("title", title);
        b.putString("content", content);
        b.putString("done", done);
        b.putString("date", date);
        intent.putExtras(b);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.YEAR,mYear);
        calendar.set(Calendar.MONTH, mMonth - 1);  // month 0 for january, 1 for feb, etc...
        calendar.set(Calendar.DAY_OF_MONTH, mDay - 1); // noitify the user one day early
        calendar.set(Calendar.HOUR_OF_DAY, mHour);
        calendar.set(Calendar.MINUTE, mMinute);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    public void saveDialog() {
        // ask user if they want to save the task, and navigave to the main activity on either selection
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Save")
                .setMessage("Do you want to save the task?")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        View v = null;
                        onClickSave(v);
                        Intent intent = new Intent(TaskContent.this, HomeActivity.class);
                        startActivity(intent);
                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(TaskContent.this, HomeActivity.class);
                        startActivity(intent);
                    }
                })
                .create();
        dialog.show();
    }

    public void connectionToBroadcastReceiver() {
        //Context registered broadcast receiver for finding connectivity actions
        myConnBR = new ConnectivityBroadcastReceiver(muConnect);
        //Set up the Intent filter to listen for those actions
        IntentFilter myFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        //Register the filter with that broadcast receiver
        this.registerReceiver(myConnBR, myFilter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                saveDialog();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    //Function to save a note to a file as a serialized object
    void saveNoteToFile(String updateOrNew) {
        // getting all the current data
        String currentId = String.valueOf(ID);
        String currentTitle = String.valueOf(edTitle.getText());
        String currentContent = String.valueOf(content.getText());
        String currentDone = String.valueOf(done.isChecked());
        String currentDate = String.valueOf(date.getText());

        // create an array list of note
        ArrayList<Note> notes = new ArrayList<>();

        // create a note with current data
        Note myNote = new Note(updateOrNew, currentId, currentTitle, currentContent, currentDone, currentDate);
        // add note to the arraylist
        notes.add(myNote);

        // getting all the input object from input stream
        try {
            //Open the file
            FileInputStream fis = this.openFileInput(TaskContent.LOCAL_FILE_NAME);
            //Open a wrapper class to read serialized objects from that file
            ObjectInputStream is = new ObjectInputStream(fis);

            Object obj = null;
            // if there is object inobject input stream, if will add to the arraylist
            while ((obj = is.readObject()) != null) {
                if (obj instanceof Note) {
                    notes.add(((Note) obj));
                }
            }

            // close the streams
            is.close();
            fis.close();
        } catch (java.io.FileNotFoundException e) {
            Log.e("open", "File Not Found. FileNotFoundException: " + e.toString());
        } catch (java.io.IOException e) {
            Log.e("open", "Cannot open file, IOException" + e.toString());
        } catch (java.lang.ClassNotFoundException e) {
            Log.e("open", "Cannot open file, Class does not exist: " + e.toString());
        }


        try {
            FileOutputStream fos = this.openFileOutput(LOCAL_FILE_NAME, Context.MODE_PRIVATE);
            //ObjectOutputStream - Takes serializeable objects and prints them to the file
            ObjectOutputStream os = new ObjectOutputStream(fos);
            //Print serialized object to file
            for (int i = 0; i < notes.size(); i++)
                os.writeObject(notes.get(i));

            os.close();
            fos.close();
        } catch (java.io.FileNotFoundException e) {
            Log.e("File", "File Not Found. FileNotFoundException: " + e.toString());
        } catch (java.io.IOException e) {
            Log.e("File", "Cannot open file, IOException" + e.toString());
        }

        if (updateOrNew.equals("1"))
            Toast.makeText(getApplicationContext(), "No connection, saved the new to file", Toast.LENGTH_LONG).show();
        else if (updateOrNew.equals("0"))
            Toast.makeText(getApplicationContext(), "No connection, saved the update to file", Toast.LENGTH_LONG).show();

        //take you back to mainactivity
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClickDate(View v)
    {
        // initialize date picker dialog
        DatePickerDialog dialog = new DatePickerDialog(
                TaskContent.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                null,
                mYear,mMonth-1,mDay);
        dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String monthString = "";
                String dayString = "";

                // month increase by 1 because 0 is january,
                month = month + 1;
                mYear = year;
                mMonth = month;
                mDay = day;

                // modify the month and day, so 1 will be 01, 2 will be 02, etc..
                if(month < 10)
                    monthString = "0" + month;
                else
                    monthString = "" + month;

                if(day < 10)
                    dayString = "0" + day;
                else
                    dayString = "" + day;

                String time = monthString + "/" + dayString + "/" + year;
                // call the time picker on click yes and pass the day selction
                timePicker(time);
            }

        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void timePicker(final String time)
    {
        // create the time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hour,int minute) {
                        String hourString = "";
                        String minuteString = "";

                        mHour = hour;
                        mMinute = minute;

                        // modify the hour and minutes, so 1 will be 01, 2 will be 02, etc..
                        if(hour < 10)
                            hourString = "0" + hour;
                        else
                            hourString = "" + hour;

                        if(minute < 10)
                            minuteString = "0" + minute;
                        else
                            minuteString = "" + minute;

                        date.setText(time+" "+ hourString + ":" + minuteString);
                    }
                }, mHour, mMinute, true); // mhour and minute is the default selection, true means 24 hours format
        timePickerDialog.show();
    }

}
