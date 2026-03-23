package org.example.project2_webdev_server.Entity;

import java.security.Timestamp;

public class Post {
    private int id;
    private String author; // username
    private String text;
    private Timestamp timeStamp;

    public Post() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {return author;}

    public void setAuthor(String author) {this.author = author;}
}
