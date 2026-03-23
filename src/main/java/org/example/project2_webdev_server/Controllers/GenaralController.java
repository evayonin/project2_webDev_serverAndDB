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
            }
            if (this.dbManager.createUserOnDb(user)) {
                success = true;
                errorCode = null;
            }
        }
        return new BasicResponse(success, errorCode);
    }
    //שינויים: במחלקה של הגיבוב שיניתי שזה יגבב *רק* סיסמה, ושיניתי במתודת הרשמה שזה מקבל אובייקט ויוצר סיסמה מגובבת ומחזיר בייסיק רספונס. 18.03.2026


    @PostMapping("/sign-in")
    public BasicResponse signIn(@RequestBody User user) {
        boolean success = false;
        Integer errorCode = ERROR_NO_ACCOUNT;
        if (user != null) {
            if(user.getUsername() == null || user.getUsername().trim().isEmpty()){
                errorCode = ERROR_EMPTY_FIELD;
            }
            else if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                errorCode = ERROR_EMPTY_FIELD;
            }
            // להתחברות
            String token = GeneralUtils.hashPassword(user.getPassword()); // גיבוב הססמה בלבד
            dbManager.updateUserToken(user.getUsername(), token);
            if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                if (this.dbManager.getUserByUsernameAndPassword(user.getUsername(), token)) {
                    success = true;
                    errorCode = null;
                }
            }
            return new LoginResponse(success, errorCode, token);
        }
        return new BasicResponse(success, errorCode);
    }


}
