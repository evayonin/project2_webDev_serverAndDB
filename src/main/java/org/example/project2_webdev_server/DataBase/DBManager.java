package org.example.project2_webdev_server.DataBase;
import jakarta.annotation.PostConstruct;
import org.example.project2_webdev_server.Entity.Post;
import org.example.project2_webdev_server.Entity.User;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DBManager {
    private static final String URL = "jdbc:mysql://localhost:3306/project2";   // אנה שימי לב! כשנגדיר את הדאטה בייס זה יהיה עם הפרטים האלה וחייב לקרוא לסכמה "project2"
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1234"; // לשנות לססמה שלי

    private Connection connection;

    @PostConstruct
    public void connect() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("DB connected successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public boolean createUserOnDb (User user) { // להרשמה
        boolean success = true;
        try {
            PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT INTO users (username, password)" +
                            "VALUE (?, ?)");
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.executeUpdate(); // יש שינוי בטבלה לכן אפדייט ולא קוורי כי לא חוזר משהו
        } catch (SQLException e) {
            success = false;
            throw new RuntimeException(e);
        }
        return success;
    }


    public boolean checkIfUsernameExists (String username) { // בדיקה אם היוזר קיים להרשמה
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement(
                            "SELECT username FROM users " +
                                    "WHERE username = ?");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean getUserByUsernameAndPassword (String username, String password) { //להתחברות
        boolean success = false;
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement(
                            "SELECT username, password FROM users " +
                                    "WHERE username = ? " +
                                    "AND password = ?");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password); // נשמר מה שמגובב ששולחים מהמתודה עם הנתיב בקונטרולר
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                success=true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return success;
    }


    public List<String> getFollowing (String username){ // תחזיר את רשימת האנשים שהיוזר המחובר עוקב אחריהם
        List<String> following = new ArrayList<>();
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement(
                             "SELECT followed_username " +
                                     "FROM follows " +
                                     "WHERE follower_username = ?;");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                following.add(resultSet.getString("followed_username"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return following;
    }


    public List<String> getFollowers (String username){ // תחזיר את רשימת האנשים שעוקבים אחרי היוזר המחובר
        List<String> followers = new ArrayList<>();
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement(
                            "SELECT follower_username " +
                                    "FROM follows " +
                                    "WHERE followed_username = ?;");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                followers.add(resultSet.getString("follower_username"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return followers;
    }


    public boolean updateUserProfileImage(String username, String imageUrl) {
        boolean success = true;
      // לא צריך את אותה בדיקה שכבר יש בקונטרולר כי הפרמטרים לא יכולים להיות ריקים
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("UPDATE users " +
                            "SET profile_image_url = ? " +
                            "WHERE username = ?");
                    preparedStatement.setString(1,imageUrl);
                    preparedStatement.setString(2,username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }


    public User getUserByToken (String token) {//מונע כל פעם בשדבורד קונטרולר לעשות כל פעם בדיקות אם המשתמש קיים ובשאר השאילתות לא צריך טוקן
        if(token == null || token.isEmpty()) {
            return null;
        }
        User user = null;
        String sql = "SELECT * FROM users WHERE token = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, token);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user = new User();
                    user.setUsername(resultSet.getString("username"));
                    user.setImageURL(resultSet.getString("profile_image_url"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }


    public boolean updateUserToken(String username, String token) {
        boolean success = true;
        String sql = "UPDATE users SET token = ? WHERE username = ?";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setString(1, token);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }


    public boolean followUser(String followerUsername, String followedUsername) { //מתוקן
        if (followerUsername == null || followerUsername.trim().isEmpty() ||
                followedUsername == null || followedUsername.trim().isEmpty()) {
            System.out.println("followUser failed: empty input");
            return false;
        }
        String sql = "INSERT INTO follows (follower_username, followed_username) VALUES (?, ?)";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            System.out.println("trying insert:"); // בדיקה שהגיע לפה
            System.out.println("follower = [" + followerUsername + "]");
            System.out.println("followed = [" + followedUsername + "]");

            ps.setString(1, followerUsername.trim()); // חייב trim אם הוכנס רווח!
            ps.setString(2, followedUsername.trim());

            int rows = ps.executeUpdate();
            System.out.println("rows inserted = " + rows); // בדיקה שהגיע לפה
            return rows == 1; // אם נוספה שורה בטבלת המעקב בupdate יחזיר true

        } catch (SQLException e) {
            System.out.println("SQL state: " + e.getSQLState());
            System.out.println("Error code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // בשביל unfollow:
    // צריך אותה אם יהיה toggle בכפתור follow (אבל אפשר לעשות יותר קל ופשוט לעשות שהיוזר יכול להוריד עוקב מהרשימת following של הפרופיל ואז בוודאות עוקב אחריו ואז לא צריך לשנות את מה ש searchUsers מחזירה) אבל נשאר כשכבת הגנה
    public boolean isFollowing(String followerUsername, String followedUsername) {
        if (followerUsername == null || followerUsername.trim().isEmpty() ||
                followedUsername == null || followedUsername.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM follows WHERE follower_username = ? AND followed_username = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, followerUsername.trim());
            ps.setString(2, followedUsername.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean unfollowUser(String followerUsername, String followedUsername) {
        if (followerUsername == null || followerUsername.trim().isEmpty() ||
                followedUsername == null || followedUsername.trim().isEmpty()) {
            return false;
        }
        String sql = "DELETE FROM follows WHERE follower_username = ? AND followed_username = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, followerUsername.trim());
            ps.setString(2, followedUsername.trim());
            int rowsDeleted = ps.executeUpdate();
            return rowsDeleted == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Post> getPostsByAuthor(String username) { // מתוקן
        List<Post> posts = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            return posts;
        }
        String sql = "SELECT id, author_username, content, created_at " +
                "FROM posts " +
                "WHERE author_username = ? " +
                "ORDER BY created_at DESC";

        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setAuthor(rs.getString("author_username"));
                    post.setText(rs.getString("content"));
                    post.setTimeStamp(rs.getTimestamp("created_at"));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return posts;
    }


    public Post createPost(String username, String content) {
        if (username == null || username.trim().isEmpty() ||
                content == null || content.trim().isEmpty()) {
            return null;
        }
        String sql = "INSERT INTO posts (author_username, content) VALUES (?, ?)";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, content);

            int affectedRows = ps.executeUpdate(); // בדיקה
            if (affectedRows == 0) {
                return null;
            }

            // מקבלים את ה- auto increment id שנוצר כדי לשמור אותו באובייקט של הפוסט:
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);

                    Post post = new Post();
                    post.setId(id);
                    post.setAuthor(username.trim());
                    post.setText(content.trim());
                    post.setTimeStamp(new java.sql.Timestamp(System.currentTimeMillis())); // הגדרה ידנית של טיים סטאמפ לפי הרגע שנוסף הפוסט לטבלה
                    return post;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Post> getFeedPosts(String username, int limit) {
        List<Post> posts = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            return posts;
        }
        String sql =
                "SELECT p.id, p.author_username, p.content, p.created_at, u.profile_image_url " +
                        "FROM posts p " +
                        "JOIN follows f ON p.author_username = f.followed_username " +
                        "JOIN users u ON p.author_username = u.username " +
                        "WHERE f.follower_username = ? " +
                        "ORDER BY p.created_at DESC " +
                        "LIMIT ?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setAuthor(rs.getString("author_username"));
                    post.setText(rs.getString("content"));
                    post.setTimeStamp(rs.getTimestamp("created_at"));
                    post.setAuthorProfileImage(rs.getString("profile_image_url"));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return posts;
    }


    public List<String> searchUsers(String query, String username) { // חיפוש משתמשים לעקוב אחריהם
        List<String> users = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return users; // יחזיר רשימה ריקה
        }
        String sql = "SELECT username FROM users " +
                "WHERE username LIKE ? " +
                "AND username <> ? " + // <> אומר שהערך השמאלי לא שווה לימני - כלומר היוזר לא יכול לחפש את עצמו
                "LIMIT 10";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, query.trim() + "%"); // היוזרניים מתחיל בתווים שיש בquery (% בסוף)
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return users;
    }





    /*
    בסה״כ יהיו לנו 3 טבלאות - טבלת יוזרים, טבלת עוקבים-נעקבים וטבלת פוסטים של יוזרים

CREATE TABLE users
(
    id                INT AUTO_INCREMENT PRIMARY KEY,
    username          VARCHAR(50) UNIQUE NOT NULL,
    password          VARCHAR(100)       NOT NULL,
    profile_image_url VARCHAR(255),
    token VARCHAR(255)
);

CREATE TABLE follows (
                         follower_username VARCHAR(50) NOT NULL,
                         followed_username VARCHAR(50) NOT NULL,

                         PRIMARY KEY (follower_username, followed_username),

                         FOREIGN KEY (follower_username)
                             REFERENCES users(username)
                             ON DELETE CASCADE,

                         FOREIGN KEY (followed_username)
                             REFERENCES users(username)
                             ON DELETE CASCADE
);

CREATE TABLE posts (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       author_username VARCHAR(50) NOT NULL,
                       content TEXT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       FOREIGN KEY (author_username)
                           REFERENCES users(username)
                           ON DELETE CASCADE
);

     */
}
