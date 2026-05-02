package org.example.project2_webdev_server.Controllers;

import org.example.project2_webdev_server.DataBase.DBManager;
import org.example.project2_webdev_server.Response.BasicResponse;
import org.example.project2_webdev_server.Response.BooleanResponse;
import org.example.project2_webdev_server.Entity.User;
import org.example.project2_webdev_server.Response.LoginResponse;
import org.example.project2_webdev_server.Utils.Errors;
import org.example.project2_webdev_server.Utils.GeneralUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.example.project2_webdev_server.Utils.Errors.*;

@RestController
public class GenaralController {

    @Autowired
    private DBManager dbManager;


    @PostMapping("/sign-up")
    public BasicResponse addUser(@RequestBody User user) { // אנוטציה ריקווסט פאראם ולא באדי כי בצד לקוח שולחים משתנים ולא אובייקט עם שדות בבקשת פוסט
        boolean success = false;
        Integer errorCode = ERROR_NO_ACCOUNT;
        if (user != null && user.getUsername() != null) { // נבדוק שהוכנס שפ משתמש
            if (!this.dbManager.checkIfUsernameExists(user.getUsername())) {
                user.setPassword(GeneralUtils.hashPassword(user.getPassword()));// יופיע בעמודת הססמה בdb
                if (this.dbManager.createUserOnDb(user)) {
                    success = true;
                    errorCode = null;
                }
            }
        }
        return new BasicResponse(success, errorCode);
    }

    @PostMapping("/sign-in")
    public LoginResponse signIn(@RequestBody User user) {
        if (user == null) {
            return new LoginResponse(false, ERROR_EMPTY_FIELD, null);
        }
        String username = user.getUsername();
        String password = user.getPassword();
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return new LoginResponse(false, ERROR_EMPTY_FIELD, null);
        }
        username = username.trim();
        password = password.trim();
        String hashedPassword = GeneralUtils.hashPassword(password); // הססמה ששמורה במסד הנתונים
        boolean isValidUser = dbManager.getUserByUsernameAndPassword(username, hashedPassword); // בדיקה שהיוזר תקין במסד הנתונים
        if (!isValidUser) {
            return new LoginResponse(false, ERROR_WRONG_CREDENTIALS, null);
        }
        // יצירת טוקן לאחר ההתחברות ושמירה בטבלה לבקשות עתידיות באמצעותו:
        String token = java.util.UUID.randomUUID().toString();

        if (!dbManager.updateUserToken(username, token)) {
            return new LoginResponse(false, ERROR_UPDATE_TOKEN_FAILED, null);
        }
        return new LoginResponse(true, null, token);
    }

}
