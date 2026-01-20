// DRAFT - לפני כתיבת הצד לקוח (הקומפוננטות של דשבורד)
// אלה הבקשות שאני יודעת שיהיו לי שהקומפוננטה של הדשבורד תנהל אותן (הן יהיו ממנה)
// ז״א כל הבקשות שיהיו כאן אלה בקשות שניתן לשלוח אחרי שההתחברות (sign in) הצליחה והיוזר הועבר לדשבורד.
// והדשבורד יכיל את תתי הקומפוננטות של הפרופיל, הפוסטים של היוזר והפיד של היוזר - רק מבחינת הui
package org.example.project2_webdev_server.Controllers;

import org.example.project2_webdev_server.DataBase.DBManager;
import org.example.project2_webdev_server.Entity.User;
import org.example.project2_webdev_server.Response.BasicResponse;
import org.example.project2_webdev_server.Response.LoginResponse;
import org.example.project2_webdev_server.Response.ObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.example.project2_webdev_server.Utils.Errors.*;

@RestController
public class DashboardController {

    @Autowired
    private DBManager dbManager;


    @RequestMapping("/dashboard/profile") // הצגת הפרופיל של היוזר
    public BasicResponse getProfile(
            @RequestParam String username,
            @RequestParam String password
    ) {
        boolean exists = dbManager.checkIfUsernameExists(username);
        if (!exists) return new BasicResponse(false, ERROR_NO_ACCOUNT);

        User user = dbManager.getUserByUsername(username ,password); // תוסיפי מתודה כזו אם אין
        return new LoginResponse(true, null, user);
    }


    @RequestMapping("/dashboard/profile-image") // הוספת תמונת פרופיל
    public BasicResponse updateProfileImage(@RequestParam String username, @RequestParam String password, @RequestParam String imageUrl) {
        boolean exists = dbManager.checkIfUsernameExists(username);
        if (!exists) return new BasicResponse(false, ERROR_NO_ACCOUNT);

        if (imageUrl == null || imageUrl.isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_IMAGE_URL);
        }
        dbManager.updateUserProfileImage(username, imageUrl); // צריך להוסיף את המתודה בדאטה בייס מנג׳ר
        return new BasicResponse(true, null);
    }


    @RequestMapping("/dashboard/following") // יחזיר את רשימת היוזים שהיוזר עוקב אחריהם
    public BasicResponse getFollowing(@RequestParam String username, @RequestParam String password) {
        boolean exists = dbManager.checkIfUsernameExists(username);
        if (!exists) return new BasicResponse(false, ERROR_NO_ACCOUNT);

        List<String> following = dbManager.getFollowing(username, password);

        // פתרון פשוט: להחזיר Map (Spring יחזיר JSON)
        return new ObjectResponse(true, null, following);
    }


    @RequestMapping("/dashboard/follow") //כשהיוזר רוצה לעשות עוקב ליוזר אחר
    public BasicResponse followUser(@RequestParam String username, @RequestParam String password, @RequestParam String targetUsername) {
        boolean exists = dbManager.checkIfUsernameExists(username);
        if (!exists) return new BasicResponse(false, ERROR_NO_ACCOUNT);

        if (targetUsername == null || targetUsername.isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_USERNAME);
        }

        if (username.equals(targetUsername)) {
            return new BasicResponse(false, ERROR_CANNOT_FOLLOW_YOURSELF);
        }

        boolean targetExists = dbManager.checkIfUsernameExists(targetUsername);
        if (!targetExists) {
            return new BasicResponse(false, ERROR_NO_ACCOUNT);
        }

        dbManager.addFollow(username, targetUsername); // צריך להוסיף את המתודה (זה יהיה אפדייט)
        return new BasicResponse(true, null);
    }


    @RequestMapping("/dashboard/my-posts") // יציג ליוזר את הפוסטים שלו
    public BasicResponse getMyPosts(
            @RequestParam String username,
            @RequestParam String password
    ) {
        boolean exists = dbManager.checkIfUsernameExists(username);
        if (!exists) return new BasicResponse(false, ERROR_NO_ACCOUNT);

        List<Map<String, Object>> posts = dbManager.getPostsByAuthor(username); // צריך להוסיף את המתודה
        return new ObjectResponse(true, null, posts);
    }


    @RequestMapping("/dashboard/new-post") // כשהיוזר יוצר פוסט חדש
    public BasicResponse createPost(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String content
    ) {
        boolean exists = dbManager.checkIfUsernameExists(username);
        if (!exists) return new BasicResponse(false, ERROR_NO_ACCOUNT);

        if (content == null || content.trim().isEmpty()) {
            return new BasicResponse(false, ERROR_MISSING_POST_CONTENT);
        }

        Map<String, Object> created = dbManager.createPost(username, content.trim()); // צריך להוסיף את המתודה
        return new ObjectResponse(true, null, created);
    }


    @RequestMapping("/dashboard/feed") // הצגת הפיד של היוזר שמכיל את 20 הפוסטים האחרונים של היוזרים שהוא עוקב אחריהם
    public BasicResponse getFeed(
            @RequestParam String username,
            @RequestParam String password
    ) {
        boolean exists = dbManager.checkIfUsernameExists(username);
        if (!exists) return new BasicResponse(false, ERROR_NO_ACCOUNT);

        List<Map<String, Object>> feed = dbManager.getFeedPosts(username, 20); // צריך להוסיף את המתודה
        return new ObjectResponse(true, null, feed);
    }
}

