package Util;

import android.app.Application;

public class JournalAPI extends Application {
    private static String userEmail, userID;
    private static JournalAPI instance;

    public JournalAPI(){}

    public static JournalAPI getInstance(){
        if(instance == null)
            instance = new JournalAPI();
        return instance;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userName) {
        JournalAPI.userEmail = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        JournalAPI.userID = userID;
    }
}
