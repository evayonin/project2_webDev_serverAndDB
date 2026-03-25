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
import org.example.project2_webdev_server.Response.UserProfileResponse;
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

    private User authenticateUser(String token) { // המנעות מכפל קוד בכל המתודות
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return dbManager.getUserByToken(token.trim());
    }

    @RequestMapping("/dashboard/profile") // מתוקן!
    public BasicResponse getProfile(@RequestHeader("Authorization") String token) {
        User user = authenticateUser(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }
        UserProfileResponse profile = new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getImageURL(),
                user.getFollowers(),
                user.getFollowing()
        );
        return new ObjectResponse(true, null, profile); // מחזיר אובייקט פרופיל בלי המידע הרגיש של היוזר
    }

    @RequestMapping("/dashboard/profile-image")
    public BasicResponse updateProfileImage(
            @RequestHeader("Authorization") String token,
            @RequestParam String imageUrl
    ) {
        User user = authenticateUser(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }
        if (imageUrl == null || imageUrl.isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_IMAGE_URL);
        }
        boolean success = dbManager.updateUserProfileImage(user.getUsername(), imageUrl.trim());
        if (!success) {
            return new BasicResponse(false, ERROR_UPDATE_PROFILE_IMAGE_FAILED);
        }
        return new BasicResponse(true, null);
    }


    @RequestMapping("/dashboard/followers")
    public BasicResponse getFollowers(@RequestHeader("Authorization") String token) { // העוקבים שלי
        User user = authenticateUser(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        List<String> followers = dbManager.getFollowers(user.getUsername()); // לבדוק אם צריך שנות!!!!
        return new ObjectResponse(true, null, followers);
    }


    @RequestMapping("/dashboard/following")
    public BasicResponse getFollowing(@RequestHeader("Authorization") String token) { // הנעקבים שלי
        User user = authenticateUser(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        List<String> following = dbManager.getFollowing(user.getUsername()); // לבדוק אם צריך שנות!!!!
        return new ObjectResponse(true, null, following);
    }


    @RequestMapping("/dashboard/follow")
    public BasicResponse followUser(  // מעקב אחרי משתמש חדש
            @RequestHeader("Authorization") String token,
            @RequestParam String targetUsername
    ) {
        User user = authenticateUser(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        if (targetUsername == null || targetUsername.isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_USERNAME);
        }
        targetUsername = targetUsername.trim(); // תקין!

        if (user.getUsername().equals(targetUsername)) {
            return new BasicResponse(false, ERROR_CANNOT_FOLLOW_YOURSELF);
        }

        boolean targetExists = dbManager.checkIfUsernameExists(targetUsername);
        if (!targetExists) {
            return new BasicResponse(false, ERROR_NO_ACCOUNT);
        }

        boolean success = dbManager.followUser(user.getUsername(), targetUsername);
        if (!success) {
            return new BasicResponse(false, ERROR_FOLLOW_FAILED); //לשנות שם
        }

        return new BasicResponse(true, null);
    }


    @RequestMapping("/dashboard/unfollow")
    public BasicResponse unfollowUser(
            @RequestHeader("Authorization") String token,
            @RequestParam String targetUsername
    ) {
        User user = authenticateUser(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_USERNAME);
        }
        targetUsername = targetUsername.trim();

        if (user.getUsername().equals(targetUsername)) {
            return new BasicResponse(false, ERROR_CANNOT_UNFOLLOW_YOURSELF);
        }
        if (!dbManager.isFollowing(user.getUsername(), targetUsername)) {
            return new BasicResponse(false, ERROR_NOT_FOLLOWING);
        }
        boolean success = dbManager.unfollowUser(user.getUsername(), targetUsername);
        if (!success) {
            return new BasicResponse(false, ERROR_UNFOLLOW_FAILED);
        }

        return new BasicResponse(true, null);
    }


    @RequestMapping("/dashboard/my-posts")
    public BasicResponse getMyPosts(@RequestHeader("Authorization") String token) throws SQLException {
        User user = authenticateUser(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }

        List<Post> posts = dbManager.getPostsByAuthor(user.getUsername());
        return new ObjectResponse(true, null, posts);
    }


    @RequestMapping("/dashboard/new-post")
    public BasicResponse createPost(  // הוספת פוסט חדש שלי
            @RequestHeader("Authorization") String token,
            @RequestParam String content
    ) {
        User user = authenticateUser(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }
        if (content == null || content.trim().isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_POST_CONTENT);
        }
        Post post = dbManager.createPost(user.getUsername(), content); // תיקנתי והוספתי

        if (post == null) {
            return new ObjectResponse(false, ERROR_CREATE_POST_FAILED, null);
        }
        return new ObjectResponse(true, null, post);
    }


    @RequestMapping("/dashboard/feed") // 20 הפוסטים של הנעקבים שלי (רשימה)
    public BasicResponse getFeed(@RequestHeader("Authorization") String token) {
        User user = authenticateUser(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }
        List<Post> feed = dbManager.getFeedPosts(user.getUsername(), 20);
        return new ObjectResponse(true, null, feed);
    }


    @RequestMapping("/dashboard/search-users") // תחזור רשימת שמות המשתמשים הרלוונטים למה שהוכנס חיפוש
    public BasicResponse searchUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam String query // התווים שהיוזר הכניס לחיפוש משתמשים
    ) {
        User user = authenticateUser(token);
        if (user == null) {
            return new BasicResponse(false, ERROR_MISSING_INVALID_TOKEN);
        }
        if (query == null || query.trim().isEmpty()) {
            return new ObjectResponse(true, null, List.of()); // אם אין יוזרים רלוונטים ליחופש יביא רשימה ריקה עם אורך 0 בהתאם למה שבלקוח (לא יציג כלום)
        }
        List<String> users = dbManager.searchUsers(query.trim(), user.getUsername());
        return new ObjectResponse(true, null, users);
    }

}