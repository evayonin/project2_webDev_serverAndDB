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



    public List<String> getFollowing (String username, String password){ // תחזיר את רשימת האנשים שהיוזר עוקב אחריהם
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

    public List<String> getFollowers (String username, String password){ // תחזיר את רשימת האנשים שעוקבים אחרי אותו יוזר מסוים
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

    public boolean updateUserProfileImage(String token, String imageUrl) {//עדיף לשלוח טוקן, כך נמנע מכפל קוד ובדיקות מיותרות שאם המשתמש קיים וכו וכו
        if(token == null || token.isEmpty() || imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("UPDATE users " +
                            "SET profile_image_url = ? " +
                            "WHERE token = ?");//תהיה עמודה של טוקן על כל משתמש
                    preparedStatement.setString(1,imageUrl);
                    preparedStatement.setString(2,token);
            int rowsUpdated = preparedStatement.executeUpdate();//בודק אם הצליח
            return rowsUpdated ==1;//אם חזר 1, מצא שורה כזאת ועדכן
    } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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

   public Map<String, List<Post>> getPostsByAuthor(String username) throws SQLException {
       Map<String, List<Post>> postsMap = new HashMap<>();
       List<Post> userPosts = new ArrayList<>();
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM posts WHERE author_username = ?")) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Post post = new Post();
                post.setId(resultSet.getInt("id"));
                post.setAuthor(resultSet.getString("author_username"));
                post.setText(resultSet.getString("text"));
                userPosts.add(post);
              }
            postsMap.put(username, userPosts);
        } catch (SQLException e) {
            e.printStackTrace();
        }
       return postsMap;
   }



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
