package com.example.dbtest;

public class sessionToken {
    private String consoleUsername;
    private String consolePassword;
    private String consoleID;
    private String consoleEmail;
    private String consolePermissions;
    public String getConsoleUsername() {
        return consoleUsername;
    }
    public String getConsolePassword() {
        return consolePassword;
    }
    public String getConsoleUserID() {
        return consoleID;
    }
    public void setConsoleUsername(String username) {
        consoleUsername = username;
    }
    public void setConsoleUserPassword(String password) {
        consolePassword = password;
    }
    public void setConsoleUserID(String ID) {
        consoleID = ID;
    }
    public String getConsoleEmail() {
        return consoleUsername;
    }
    public String getConsolePermissions() {
        return consolePassword;
    }
    public void setConsoleEmail(String email) {
        consoleEmail = email;
    }
    public void setConsoleUserPermissions(String permissions) {
        consolePermissions = permissions;
    }
}
