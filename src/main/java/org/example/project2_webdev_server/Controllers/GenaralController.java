package org.example.project2_webdev_server.Controllers;

import org.example.project2_webdev_server.DataBase.DBManager;
import org.example.project2_webdev_server.Entity.User;
import org.example.project2_webdev_server.Response.BasicResponse;
import org.example.project2_webdev_server.Response.BooleanResponse;
import org.example.project2_webdev_server.Response.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.example.project2_webdev_server.Utils.Errors.*;

@RestController
public class GenaralController {

    @Autowired
    private DBManager dbManager;

    @RequestMapping("/hello") // בדיקה מהעמוד סיין אין בלקוח
    public void sayHello(){
        System.out.println("hello");
    }


    @RequestMapping("sign-up")
    public BasicResponse addUser (String username, String password) {
        if (username != null && !username.isEmpty()) {
            if (password != null && !password.isEmpty()) {
                User user = new User(username, password);
                dbManager.createUserOnDb(user);
                return new BasicResponse(true, null);
            } else {
                return new BasicResponse(false, ERROR_MISSING_PASSWORD);
            }
        } else {
            return new BasicResponse(false, ERROR_MISSING_USERNAME);
        }
    }


    @RequestMapping("is-username-available")
    public BasicResponse isUsernameAvailable (String username) { // האם היוזר כבר קיים או לא (העת הרשמה)
        if (username != null && !username.isEmpty()) {
            boolean available = dbManager.isUsernameAvailable(username);
            return new BooleanResponse(true, null, available); // תחזור גם תשובה בוליאנית אם ניתן להרשם עם השם משתמש הזה
        } else {
            return new BasicResponse(false, ERROR_MISSING_USERNAME); // אם לא ניתן תחזור שגיאה רגילה
        }
    }


    @RequestMapping("sign-in")
    public BasicResponse signIn (String username, String password) { // ללא אותנטיקציה
        if (username != null && !username.isEmpty()) {
            if (password != null && !password.isEmpty()) {
                User user = dbManager.getUserByUsernameAndPassword(username, password); // אם יש יוזר כזה מחזיר אותו אם אין חוזר נאל
                if (user != null) { // אם חזר יוזר (קיים כזה)
                    return new LoginResponse(true, null, user); // הכניסה לחשבון הצליחה נחזיר תשובה בהתאם
                }
                else { // אם חזרה רשומה נאל מהדאטה בייס (כביכול לא קיים יוזר כזה)
                    boolean usernameExists = dbManager.checkIfUsernameExists(username); // קודם נבדוק אם בכלל הזין שם משתמש שקיים במערכת
                    if(usernameExists){ // אם השם משתמש הזה כן קיים זה אומר שהססמה שהזין לא נכונה
                        return new BasicResponse(false, ERROR_WRONG_PASSWORD);
                    }
                    else {// אם חזר מהדאטה בייס יוזר נאל וגם בוליאן(שם משתמש) נאל אז זה אומר שבאמת לא קיים יוזר כזה
                        return new BasicResponse(false, ERROR_NO_ACCOUNT);
                    }
                }
            } else { // אם לא הצליח להתחבר כי לא הוכנסה ססמה (היא נאל)
                return new BasicResponse(false, ERROR_MISSING_PASSWORD);
            }
        } else { // אם לא הצליח להתחבר כי לא הוכנס שם משתמש (הוא נאל)
            return new BasicResponse(false, ERROR_MISSING_USERNAME);
        }
    }
}
