package com.csce4623.ahnelson.todolist;

/**
 * Created by tongyu on 10/3/17.
 */

public class Task {
    private int id;
    private String title, content, done, date;

    Task(int id, String title, String content, String done, String date) {
        this.title = title;
        this.id = id;
        this.content = content;
        this.done = done;
        this.date = date;
    }
    // getters and setters for the values

    public void setId(int id) {
        this.id = id;
    }

    public String getId() {
        return toString(id);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String toString(int object) {
        return object + "";
    }

}
