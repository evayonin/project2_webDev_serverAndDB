package org.example.project2_webdev_server.Response;

public class ObjectResponse extends BasicResponse { // תגובות של השרת לבקשות שצריכות להחזיר אובייקטים כמו רשימת פוסטים של היוזר, הוספת פוסט חדש, פיד ורשימת נעקבים

    private Object data;

    public ObjectResponse(boolean success, Integer errorCode, Object data) {
        super(success, errorCode);
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
