package org.example.project2_webdev_server.Response;

import java.util.List;

public class UserProfileResponse {

    private Integer id;
    private String username;
    private String profileImageUrl;
    private List<String> followers;
    private List<String> following;

    public UserProfileResponse(Integer id,
                               String username,
                               String profileImageUrl,
                               List<String> followers,
                               List<String> following) {
        this.id = id;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.followers = followers;
        this.following = following;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getFollowing() {
        return following;
    }
}