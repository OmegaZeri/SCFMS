package com.example.dbtest;

public class sessionToken {
    private String consoleUsername;
    private String consolePassword;
    private int consoleID;
    private String consoleEmail;
    private int consolePermissions;
    public String getConsoleUsername() {
        return consoleUsername;
    }
    public String getConsolePassword() {
        return consolePassword;
    }
    public int getConsoleUserID() {
        return consoleID;
    }
    public void setConsoleUsername(String username) {
        consoleUsername = username;
    }
    public void setConsoleUserPassword(String password) {
        consolePassword = password;
    }
    public void setConsoleUserID(int ID) {
        consoleID = ID;
    }
    public String getConsoleEmail() {
        return consoleUsername;
    }
    public int getConsolePermissions() {
        return consolePermissions;
    }
    public void setConsoleEmail(String email) {
        consoleEmail = email;
    }
    public void setConsoleUserPermissions(int permissions) {
        consolePermissions = permissions;
    }
}
