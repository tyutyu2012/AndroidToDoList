package com.csce4623.ahnelson.todolist;

/**
 * Created by tongyu on 10/6/17.
 */

//Implement Serializable class so that the object can be saved to file
public class Note implements java.io.Serializable {
    private String updateOrNew, id, title, content, done, date;

    Note(String updateOrNew, String id, String title, String content, String done, String date) {
        this.updateOrNew = updateOrNew;
        this.id = id;
        this.title = title;
        this.content = content;
        this.done = done;
        this.date = date;
    }

    // setters and getters
    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }

    public String getDone() {
        return done;
    }

    public String getDate() {
        return date;
    }

    public String getUpdateOrNew() {
        return updateOrNew;
    }

    @Override
    public String toString() {
        return updateOrNew + ": " + id + ": " + title + ": " + content + ": " + done + ": " + date;
    }

}