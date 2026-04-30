package com.example.dbtest;

public class sessionToken {
    private String consoleUsername;
    private String consolePassword;
    private int consoleID;
    private String consoleEmail;
    private int consolePermissions;
    private Long consolePhoneNumber;
    public String getConsoleUsername() {
        return consoleUsername;
    }
    public String getConsolePassword() {
        return consolePassword;
    }
    public int getConsoleUserID() {return consoleID;}
    public Long getConsolePhoneNumber() {return consolePhoneNumber;}
    public int getConsolePermissions() {return consolePermissions;}
    public String getConsoleEmail() {return consoleUsername;}
    public void setConsoleUsername(String username) {consoleUsername = username;}
    public void setConsoleUserPassword(String password) {consolePassword = password;}
    public void setConsoleUserID(int ID) {consoleID = ID;}
    public void setConsoleEmail(String email) {consoleEmail = email;}
    public void setConsoleUserPermissions(int permissions) {consolePermissions = permissions;}
    public void setConsolePhoneNumber(Long phoneNumber){consolePhoneNumber=phoneNumber;}
}
