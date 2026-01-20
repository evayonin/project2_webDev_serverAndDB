package org.example.project2_webdev_server.Entity;

public class User {
    private int id; // יהיה הpk בטבלת יוזרים ו-auto incremented אבל כרגע לא בשימוש בשום מקום
    private String username;
    private String password;

    public User () {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public int getId() { // אולי נוריד
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
