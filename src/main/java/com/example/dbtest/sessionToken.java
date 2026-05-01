package com.example.dbtest;

public class sessionToken {
    private String consoleUsername;
    private String consolePassword;
    private int consoleID;
    private String consoleEmail;
    private int consolePermissions;
    private long consolePhoneNumber;
    private String newUsername;
    private String newUsernameLast;
    public String getConsoleUsername() {
        return consoleUsername;
    }
    public String getConsolePassword() {
        return consolePassword;
    }
    public int getConsoleUserID() {return consoleID;}
    public long getConsolePhoneNumber() {return consolePhoneNumber;}
    public int getConsolePermissions() {return consolePermissions;}
    public String getNewUsername() {return newUsername;}
    public String getNewUsernameLast() {return newUsernameLast;}
    public void setConsoleUsername(String username) {consoleUsername = username;}
    public void setConsoleUserPassword(String password) {consolePassword = password;}
    public void setConsoleUserID(int ID) {consoleID = ID;}
    public void setConsoleUserPermissions(int permissions) {consolePermissions = permissions;}
    public void setConsolePhoneNumber(long phoneNumber){consolePhoneNumber=phoneNumber;}
    public void setNewUsername(String newUsernameInput){newUsername = newUsernameInput;}
    public void setNewUserLast(String newUserLastInput){newUsernameLast = newUserLastInput;}
}
