package org.example.project2_webdev_server.DataBase;
import jakarta.annotation.PostConstruct;
import org.example.project2_webdev_server.Entity.Post;
import org.example.project2_webdev_server.Entity.User;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DBManager {
    private static final String URL = "jdbc:mysql://localhost:3306/project2";   // אנה שימי לב! כשנגדיר את הדאטה בייס זה יהיה עם הפרטים האלה וחייב לקרוא לסכמה "project2"
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1234"; // אנה כשאת מריצה אצלך שימי 1234

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


    public User getUserByUsername (String username, String password) { // שיניתי שיהיה לפי שם משתמש ולא id - יותר פשוט. בשימוש בדשבורד קונטרולר
        try {
            PreparedStatement preparedStatement = this.connection
                    .prepareStatement("SELECT username " +
                            "FROM users " +
                            "WHERE username = ?");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString(1));
                return user;
            }
        } catch (SQLException e) {

        }
        return null;
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
                                    "AND password_hash = ?");//תהיה עוד עמודה של סיסמה, ועמודה של סיסמה_האש כדי שלא יגש לשם
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


// לבדוק אם צריך לשנות את הטבלה והשאילתא
// SELECT u.username FROM follows f JOIN users u ON f.follower_id = u.id  WHERE f.follower_id = ?

    public List<String> getFollowing (String username){ // תחזיר את רשימת האנשים שהיוזר המחובר עוקב אחריהם
        List<String> following = new ArrayList<>();
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement(
                             "SELECT followed_username" +
                                     "FROM follows" +
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

    // לבדוק אם צריך לשנות את הטבלה והשאילתא
    // SELECT u.username FROM follows f JOIN users u ON f.follower_id = u.id WHERE f.followed_id = ?"

    public List<String> getFollowers (String username){ // תחזיר את רשימת האנשים שעוקבים אחרי היוזר המחובר
        List<String> followers = new ArrayList<>();
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement(
                            "SELECT follower_username" +
                                    "FROM follows" +
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


    public boolean updateUserProfileImage(String username, String imageUrl) { // מתוקן - לא שולחים עם הטוקן!
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


    public User getUserByToken (String token) {//מונע כל פעם בשדבורד קונטרולר לעשות כל פעם בדיקות אם המשתמש קיים וכו וכו, בדיקה קצרה - יש טוקן? ממשיכים. אין? נחזיר פולס
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


    public boolean followUser(String token, String targetUsername) {
        User user = getUserByToken(token);
        if(user == null||user.getUsername().equals(targetUsername)) {
            return false;
        }
        try (PreparedStatement statement = this.connection.prepareStatement("INSERT INTO follows (follower_username, followed_username) VALUES (?, ?)")) {
            statement.setString(1, user.getUsername()); //אני העוקב
            statement.setString(2, targetUsername);    //הוא הנעקב
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }


    public List<Post> getPostsByAuthor(String username) throws SQLException { // תיקנתי שיחזיר מערך פוסטים שכל פוסט מכיל את השדות שלו, לא מפה
        List<Post> posts = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            return posts;
        }
        String sql = "SELECT id, author, text, timestamp " +
                "FROM posts " +
                "WHERE author = ? " +
                "ORDER BY created_at DESC";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setAuthor(rs.getString("author"));
                    post.setText(rs.getString("text"));
                    post.setTimeStamp(rs.getTimestamp("created_at"));
                    posts.add(post);
                }
            }
        }
        return posts;
    }


    // המתודה שהוספתי:
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


    // המתודה שהוספתי:
    public List<Post> getFeedPosts(String username, int limit) {
        List<Post> posts = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            return posts;
        }
        String sql =
                "SELECT p.id, p.author_username, p.content, p.created_at " +
                        "FROM posts p " +
                        "JOIN followers f ON p.author_username = f.followed_username " +
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
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return posts;
    }





    // צריך להוסיף מתודה של search users





    /*
    צריך ליצור סכמה עם שם מתאים!
    בסה״כ יהיו לנו 3 טבלאות - טבלת יוזרים, טבלת עוקבים-נעקבים וטבלת פוסטים של יוזרים

     טבלת היוזרים שנגדיר בהמשך:
     CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(255) // יכול להיות נאל כי בהוספת יוזר חדש עדיין אין קישור בטבלה
    לא לשכוח להוסיף עמודה של טוקן!!!
);

     טבלת העוקבים-נעקבים:
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

בקשור לטבלת העוקבים - נעקבים:

שאילתא - מי עוקב אחרי יוזר מסוים:
SELECT follower_username
FROM follows
WHERE followed_username = ?;

שאילתא - מי היוזר המסויים עוקב אחריהם:
SELECT followed_username
FROM follows
WHERE follower_username = ?;



טבלת הפוסטים:

CREATE TABLE posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    author_username VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (author_username)
        REFERENCES users(username)
        ON DELETE CASCADE
);



   עוד מתודות שנצטרך להוסיף לכאן בהמשך:

void updateUserProfileImage(String username, String imageUrl) עשיתי

void addFollow(String followerUsername, String followedUsername) עשיתי, אבל לא יהיה ווייד, יחזיר בוליאן נראה לי

List<Map<String, Object>> getPostsByAuthor(String username)

Map<String, Object> createPost(String username, String content)

List<Map<String, Object>> getFeedPosts(String username, int limit)

     */
}
