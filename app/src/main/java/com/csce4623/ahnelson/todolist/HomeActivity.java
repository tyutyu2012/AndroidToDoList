package com.csce4623.ahnelson.todolist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

//Create HomeActivity and implement the OnClick listener
public class HomeActivity extends AppCompatActivity {

    private ConnectivityBroadcastReceiver myConnBR;
    private ListView mTaskListView;
    private Hashtable<String, Task> table;
    public MenuItem muConnect;

    // onCreate runs first
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_view);
        initializeComponents();
    }

    // This runs after onCreate
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // initialize the menu, the connection view
        getMenuInflater().inflate(R.menu.menu, menu);
        muConnect = menu.findItem(R.id.Connection);

        // connect to broadcaster receiver
        connectionToBroadcastReceiver();

        // checking if there is any files needed to be update
        if (myConnBR.isConnected(this))
            loadNoteFromFile();

        // update user interface
        updateUI();
        return super.onCreateOptionsMenu(menu);
    }

    void initializeComponents() {
        // initialize the listview, and a hashtable
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        table = new Hashtable<String, Task>();
    }

    public void onClickNewToDo(View v) {
        // navigate from home to new content
        Intent intent = new Intent(getApplicationContext(), TaskContent.class);
        this.startActivity(intent);
    }

    public void onClickDelete(View v) {

        // I save the id to a textview, but i made it invisible, getting the specific id here
        View parent = (View) v.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.tv_id);
        final String taskId = String.valueOf(taskTextView.getText());

        // if there is network
        if(myConnBR.isConnected(this)) {
            // alert dialog to ask user if they want to delete the task
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want DELETE this task?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // delete the value in table
                            table.remove(taskId);
                            // delete the specific task using id
                            getContentResolver().delete(ToDoProvider.CONTENT_URI, ToDoProvider.TODO_TABLE_COL_ID + "=" + taskId, null);
                            // cancel the alarm using id
                            cancelAlarm(Integer.valueOf(taskId));
                            // update user interface
                            updateUI();
                        }
                    })
                    // do nothing when clicking no
                    .setNegativeButton("No", null)
                    .show();
        }
        else
        {
            // 2 stands for delte the task, save it to the file
            saveNoteToFile("2", taskId);
        }
    }

    public void onClickTitle(View v) {
        // get the id
        View parent = (View) v.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.tv_id);
        String id = String.valueOf(taskTextView.getText());

        // get the title and content
        Task task = table.get(id);
        String title = task.getTitle();
        String content = task.getContent();
        String done = task.getDone();
        String date = task.getDate();

        // pass id, title and content to the TaskContent
        Intent intent = new Intent(HomeActivity.this, TaskContent.class);
        Bundle b = new Bundle();
        b.putInt("id", Integer.valueOf(id));
        b.putString("title", title);
        b.putString("content", content);
        b.putString("done", done);
        b.putString("date", date);
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }

    public void onClickRefresh(View v) {
        //refresh on network connection
        if(myConnBR.isConnected(this)) {
            loadNoteFromFile();
            updateUI();
            Toast.makeText(getApplicationContext(), "Refreshing the page", Toast.LENGTH_LONG).show();
        }
        // sorry message for no network connection
        else
        {
            Toast.makeText(getApplicationContext(), "Sorry, No Connection", Toast.LENGTH_LONG).show();
        }
    }

    // update the userinterface
    public void updateUI() {
        //Perform a query to get all rows in the DB
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI, ToDoProvider.projection, null, null, null);
        // create a list of task for adapter to use
        final List<Task> tasks = new ArrayList<Task>();
        // clear the hash table
        table.clear();
        // while the cursor contents information
        while (myCursor.moveToNext()) {
            // getting the index of the datefrom each column
            int idIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_ID);
            int titleIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_TITLE);
            int contentIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_CONTENT);
            int doneIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_DONE);
            int dateIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_DATE);
            // getting the data using the indexs
            String id = myCursor.getString(idIndex);
            String title = myCursor.getString(titleIndex);
            String content = myCursor.getString(contentIndex);
            String done = myCursor.getString(doneIndex);
            String date = myCursor.getString(dateIndex);

            // create a new task
            Task task = new Task(Integer.valueOf(id), title, content, done, date);

            // put the task into hashtable
            table.put(id, task);

            // add the task into an arrayList, later use for adapter
            tasks.add(task);
        }

        // create an adapter using the class I created
        TaskAdapter adapter = new TaskAdapter(this, tasks);
        // set the listview with adapter
        mTaskListView.setAdapter(adapter);
    }

    public void connectionToBroadcastReceiver() {
        //Context registered broadcast receiver for finding connectivity actions
        myConnBR = new ConnectivityBroadcastReceiver(muConnect);
        //Set up the Intent filter to listen for those actions
        IntentFilter myFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        //Register the filter with that broadcast receiver
        this.registerReceiver(myConnBR, myFilter);
    }

    private void cancelAlarm(int id)
    {
        // cancel the alarm using the id
        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationCreator.class);
        alarmMgr.cancel(PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        //Log.d("cancel", "cancelling notification");
    }

    public void alarmNotification(String id, String title, String content, String done, String date) {
        // initial a alarm manager
        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        // initial a intent, and pass all the variables, only id, title, and date is really needed.
        Intent intent = new Intent(this, NotificationCreator.class);
        Bundle b = new Bundle();
        b.putInt("id", Integer.valueOf(id));
        b.putString("title", title);
        b.putString("content", content);
        b.putString("done", done);
        b.putString("date", date);
        intent.putExtras(b);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, Integer.valueOf(id), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // split the date string into year, monlth, day, ,hours, and minutes
        String[] dateTime = date.split("\\s+");
        String[] monthDayYear = dateTime[0].split("/");
        String[] hourMinute = dateTime[1].split(":");

        int mMonth = Integer.valueOf(monthDayYear[0]);
        int mDay = Integer.valueOf(monthDayYear[1]);
        int mYear= Integer.valueOf(monthDayYear[2]);
        int mHour = Integer.valueOf(hourMinute[0]);
        int mMinute = Integer.valueOf(hourMinute[1]);

        // set the caldenter using time information.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.YEAR,mYear);
        calendar.set(Calendar.MONTH, mMonth - 1);  // month 0 for january, 1 for feb, etc...
        calendar.set(Calendar.DAY_OF_MONTH, mDay - 1); // noitify the user one day early
        calendar.set(Calendar.HOUR_OF_DAY, mHour);
        calendar.set(Calendar.MINUTE, mMinute);

        // update or schedule the new notification
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    //Function to load an object from a file
    public void loadNoteFromFile() {
        try {
            //Open the file
            FileInputStream fis = this.openFileInput(TaskContent.LOCAL_FILE_NAME);
            //Open a wrapper class to read serialized objects from that file
            ObjectInputStream is = new ObjectInputStream(fis);

            Object obj = null;

            // go throught all the objects in the input stream
            while ((obj = is.readObject()) != null) {
                if (obj instanceof Note) {
                    Note temp = (Note) obj;
                    //Create a ContentValues object
                    ContentValues myCV = new ContentValues();
                    myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, temp.getTitle());
                    myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, temp.getContent());
                    myCV.put(ToDoProvider.TODO_TABLE_COL_DONE, temp.getDone());
                    myCV.put(ToDoProvider.TODO_TABLE_COL_DATE, temp.getDate());

                    // delete a task using file
                    if (temp.getUpdateOrNew().equals("2")) {
                        getContentResolver().delete(ToDoProvider.CONTENT_URI, ToDoProvider.TODO_TABLE_COL_ID + "=" + temp.getId(), null);
                        cancelAlarm(Integer.valueOf(temp.getId()));
                        Log.d("notesss", temp.toString());
                    }
                    // adding new task
                    else if (temp.getUpdateOrNew().equals("1")) {
                        alarmNotification(String.valueOf(temp.getId()), temp.getTitle(), temp.getContent(), temp.getDone(), temp.getDate());
                        getContentResolver().insert(ToDoProvider.CONTENT_URI, myCV);

                        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI, ToDoProvider.projection, null, null, null);
                        // move to the last info added.
                        myCursor.moveToLast();
                        int idIndex = myCursor.getColumnIndex(ToDoProvider.TODO_TABLE_COL_ID);

                        alarmNotification(myCursor.getString(idIndex), temp.getTitle(), temp.getContent(), temp.getDone(), temp.getDate());
                    }
                    // update the task
                    else if (temp.getUpdateOrNew().equals("0")) {
                        // update the value to the database
                        getContentResolver().update(ToDoProvider.CONTENT_URI, myCV, ToDoProvider.TODO_TABLE_COL_ID + "=" + temp.getId(), null);
                        // update notification
                        alarmNotification(String.valueOf(temp.getId()), temp.getTitle(), temp.getContent(), temp.getDone(), temp.getDate());
                    }
                }
            }

            is.close();
            fis.close();

        } catch (java.io.FileNotFoundException e) {
            Log.e("open", "File Not Found. FileNotFoundException: " + e.toString());
        } catch (java.io.IOException e) {
            Log.e("open", "Cannot open file, IOException" + e.toString());
        } catch (java.lang.ClassNotFoundException e) {
            Log.e("open", "Cannot open file, Class does not exist: " + e.toString());
        }

        // clear the output stream with an random note
        try {
            FileOutputStream fos = this.openFileOutput(TaskContent.LOCAL_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            // random notes, does not do anything
            Note temp = new Note("5", "1", "5", "1", "5", "1");
            os.writeObject(temp);
            os.close();
            fos.close();
        } catch (java.io.FileNotFoundException e) {
            Log.e("File", "File Not Found. FileNotFoundException: " + e.toString());
        } catch (java.io.IOException e) {
            Log.e("File", "Cannot open file, IOException" + e.toString());
        }
    }

    public void saveNoteToFile(String updateOrNew, String id) {

        // create an array list of note
        ArrayList<Note> notes = new ArrayList<>();

        // create a note with current data
        Note myNote = new Note(updateOrNew, id, "", "", "", "");
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
            FileOutputStream fos = this.openFileOutput(TaskContent.LOCAL_FILE_NAME, Context.MODE_PRIVATE);
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

        // toast messasges for different intention
        if (updateOrNew.equals("2"))
            Toast.makeText(getApplicationContext(), "No connection, saved the delete to file", Toast.LENGTH_LONG).show();
        else if (updateOrNew.equals("1"))
            Toast.makeText(getApplicationContext(), "No connection, saved the new to file", Toast.LENGTH_LONG).show();
        else if (updateOrNew.equals("0"))
            Toast.makeText(getApplicationContext(), "No connection, saved the update to file", Toast.LENGTH_LONG).show();

    }
}
