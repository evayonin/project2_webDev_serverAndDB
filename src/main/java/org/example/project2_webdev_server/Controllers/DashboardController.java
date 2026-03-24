// DRAFT - לפני כתיבת הצד לקוח (הקומפוננטות של דשבורד)
// אלה הבקשות שאני יודעת שיהיו לי שהקומפוננטה של הדשבורד תנהל אותן (הן יהיו ממנה)
// ז״א כל הבקשות שיהיו כאן אלה בקשות שניתן לשלוח אחרי שההתחברות (sign in) הצליחה והיוזר הועבר לדשבורד.
// והדשבורד יכיל את תתי הקומפוננטות של הפרופיל, הפוסטים של היוזר והפיד של היוזר - רק מבחינת הui
package org.example.project2_webdev_server.Controllers;

import org.example.project2_webdev_server.DataBase.DBManager;
import org.example.project2_webdev_server.Entity.Post;
import org.example.project2_webdev_server.Entity.User;
import org.example.project2_webdev_server.Response.BasicResponse;
import org.example.project2_webdev_server.Response.LoginResponse;
import org.example.project2_webdev_server.Response.ObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.example.project2_webdev_server.Utils.Errors.*;
@RestController
public class DashboardController {

    @Autowired
    private DBManager dbManager;

    @RequestMapping("/dashboard/profile")
    public BasicResponse getProfile(@RequestHeader("Authorization") String token) {
        if (token == null || token.isEmpty()) {
            return new ObjectResponse(false, ERROR_MISSING_INVALID_TOKEN, null);
        }

        User user = dbManager.getUserByToken(token);
        if (user == null) {
            return new ObjectResponse(false, ERROR_MISSING_INVALID_TOKEN, null);
        }

        return new ObjectResponse(true, null, user);
    }

    @RequestMapping("/dashboard/profile-image")
    public BasicResponse updateProfileImage(
            @RequestHeader("Authorization") String token,
            @RequestParam String imageUrl
    ) {
        if (token == null || token.isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        if (imageUrl == null || imageUrl.isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_IMAGE_URL);
        }

        User user = dbManager.getUserByToken(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        boolean success = dbManager.updateUserProfileImage(token, imageUrl);
        if (!success) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        return new BasicResponse(true, null);
    }

    @RequestMapping("/dashboard/followers")
    public ObjectResponse getFollowers(@RequestHeader("Authorization") String token) { // העוקבים שלי
        if (token == null || token.isEmpty()) {
            return new ObjectResponse(false, ERROR_MISSING_INVALID_TOKEN, null);
        }

        User user = dbManager.getUserByToken(token);
        if (user == null) {
            return new ObjectResponse(false, ERROR_MISSING_INVALID_TOKEN, null);
        }

        List<String> followers = dbManager.getFollowers(user.getUsername());

        return new ObjectResponse(true, null, followers);
    }


    @RequestMapping("/dashboard/following")
    public BasicResponse getFollowing(@RequestHeader("Authorization") String token) { // הנעקבים שלי
        if (token == null || token.isEmpty()) {
            return new ObjectResponse(false, ERROR_MISSING_INVALID_TOKEN, null);
        }

        User user = dbManager.getUserByToken(token);
        if (user == null) {
            return new ObjectResponse(false, ERROR_MISSING_INVALID_TOKEN, null);
        }

        List<String> following = dbManager.getFollowing(user.getUsername());
        return new ObjectResponse(true, null, following);
    }

    @RequestMapping("/dashboard/follow")
    public BasicResponse followUser(
            @RequestHeader("Authorization") String token,
            @RequestParam String targetUsername
    ) {
        if (token == null || token.isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        User user = dbManager.getUserByToken(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        if (targetUsername == null || targetUsername.isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_USERNAME);
        }

        if (user.getUsername().equals(targetUsername)) {
            return new BasicResponse(false, ERROR_CANNOT_FOLLOW_YOURSELF);
        }

        boolean targetExists = dbManager.checkIfUsernameExists(targetUsername);
        if (!targetExists) {
            return new BasicResponse(false, ERROR_NO_ACCOUNT);
        }

        boolean success = dbManager.followUser(user.getUsername(), targetUsername);
        if (!success) {
            return new BasicResponse(false, ERROR_GENERAL);
        }

        return new BasicResponse(true, null);
    }

    @RequestMapping("/dashboard/my-posts")
    public BasicResponse getMyPosts(@RequestHeader("Authorization") String token) throws SQLException {
        if (token == null || token.isEmpty()) {
            return new ObjectResponse(false, ERROR_MISSING_INVALID_TOKEN, null);
        }

        User user = dbManager.getUserByToken(token);
        if (user == null) {
            return new ObjectResponse(false, ERROR_MISSING_INVALID_TOKEN, null);
        }

        Map<String, List<Post>> posts = dbManager.getPostsByAuthor(user.getUsername());
        return new ObjectResponse(true, null, posts);
    }

    @RequestMapping("/dashboard/new-post")
    public BasicResponse createPost(
            @RequestHeader("Authorization") String token,
            @RequestParam String content
    ) {
        if (token == null || token.isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        User user = dbManager.getUserByToken(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        if (content == null || content.trim().isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_POST_CONTENT);
        }

        Map<String, Object> created = dbManager.createPost(user.getUsername(), content.trim());
        return new ObjectResponse(true, null, created);
    }

    @RequestMapping("/dashboard/feed")
    public BasicResponse getFeed(@RequestHeader("Authorization") String token) {
        if (token == null || token.isEmpty()) {
            return new ObjectResponse(false, ERROR_MISSING_INVALID_TOKEN, null);
        }

        User user = dbManager.getUserByToken(token);
        if (user == null) {
            return new ObjectResponse(false, ERROR_MISSING_INVALID_TOKEN, null);
        }

        List<Map<String, Object>> feed = dbManager.getFeedPosts(user.getUsername(), 20);
        return new ObjectResponse(true, null, feed);
    }
}