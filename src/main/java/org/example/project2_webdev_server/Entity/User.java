package org.example.project2_webdev_server.Entity;

import java.util.List;

public class User {
    private int id; // יהיה הpk בטבלת יוזרים ו-auto incremented אבל כרגע לא בשימוש בשום מקום
    private String username;
    private String password;
    private String imageURL;
    private List<String> followers;
    private List<String> following;
    private String token;


    public User () {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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

    public String getToken() {return token;}

    public void setToken(String token) {this.token = token;}

}
