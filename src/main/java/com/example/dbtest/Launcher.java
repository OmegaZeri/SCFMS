package com.example.dbtest;
import com.example.dbtest.sqlHandler.*;
import com.example.dbtest.sessionToken.*;

import javax.xml.transform.Result;
import java.util.Scanner;
import java.sql.*;
import java.time.*;
public class Launcher {
    static Connection conn;
    static int accessAttempt;
    public static void main(String[] args) throws SQLException {
        dbConnector();
        login();
        //accessLogs();
        //genReport();
    }

    //function to connect program to database
    public static void dbConnector() throws SQLException {
        Scanner scan = new Scanner(System.in);
        sqlHandler sql = new sqlHandler();
        System.out.println("Please enter DB Credentials");
        sql.setSqlUser();
        String user = sql.getSqlUser();
        sql.setSqlPassword();
        String password = sql.getSqlPassword();
        String connection = sql.getSqlConnection();
        conn = DriverManager.getConnection(connection, user, password);
        if (conn.isValid(300)) {
            System.out.println("Connection Established");
        }
    }
    //Undergrad login example:    sdaniels@example.edu
    //                            'h%(758KhdmL&'
    //login function for console
    public static void login() throws SQLException {
        Scanner scan = new Scanner(System.in);
        sessionToken sT = new sessionToken();
        System.out.println("Please enter your SCFMS email: ");
        String username = scan.nextLine();
        System.out.println("Please enter your SCFMS password: ");
        String password = scan.nextLine();
        sqlHandler sql = new sqlHandler();
        PreparedStatement pstmnt = conn.prepareStatement(sql.loginQuery());
        pstmnt.setString(1, username);
        pstmnt.setString(2, password);
        ResultSet rs = pstmnt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        if (rs.next()){
            sT.setConsoleUsername(username);
            sT.setConsoleUserPassword(password);
            pstmnt = conn.prepareStatement(sql.welcomeQuery());
            pstmnt.setString(1, sT.getConsolePassword());
            ResultSet rs2 =pstmnt.executeQuery();
            rs2.next();
            System.out.println("Login Successful, welcome " + rs2.getString(1));
            sT.setConsoleUserID(rs2.getInt(2));
            accessAttempt =0;
            pstmnt = conn.prepareStatement(sql.permsQuery());
            pstmnt.setString(1, sT.getConsolePassword());
            rs = pstmnt.executeQuery();
            rs.next();
            if(rs.getInt(1) < 4 ) {
                int perms = 1;
                sT.setConsoleUserPermissions(perms);
                menu(sT.getConsolePermissions(), sT);
            }
            //Checks users permissions, and sets their console menu to the correct permissions
            else if (rs.getInt(1) == 4) {
                int perms = 4;
                sT.setConsoleUserPermissions(perms);
                menu(sT.getConsolePermissions(), sT);
            }
        }
        else if(!rs.next()){
            accessAttempt+=1;
            System.out.println("Invalid login info, try again.");
            login();
        }
        else if(accessAttempt==3){
            System.out.println("You have failed to login 3 times, client will now shutdown");
            System.exit(0);
        }
    }
    public static void menu(int i, sessionToken sT) throws SQLException {
        int perms = i;
        int choice;
        Scanner scan = new Scanner(System.in);
        if (perms == 1){
            do {
                System.out.println("------Example University Console------");
                System.out.println("      ------1. Access Logs------      ");
                System.out.println("       ------  2. Quit   ------      ");
                choice = scan.nextInt();
                switch (choice) {
                    case 1:
                        System.out.println("Pulling your logs for the last 30 days.");
                        accessLogs(sT);
                        break;
                    case 2:
                        System.out.println("Closing console...");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice, try again?");
                }
            } while (choice != 2);
        }
        else if (perms == 4){

        }
    }
    public static void accessLogs(sessionToken sT) throws SQLException {
        sqlHandler sqlHandler = new sqlHandler();
        PreparedStatement pstmnt = conn.prepareStatement(sqlHandler.logsQuery());
        pstmnt.setInt(1, sT.getConsoleUserID());
        ResultSet rsLogs = pstmnt.executeQuery();
        ResultSetMetaData rsmd = rsLogs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        boolean rowsFound= false;
        while (rsLogs.next()) {
            rowsFound= true;
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rsmd.getColumnName(i) + ": " + rsLogs.getString(i) + " ");
            }
            System.out.println();
        }
        if (!rowsFound){
            System.out.println("No logs to display, try opening some doors.");
        }
    }
    public static void genReport() throws SQLException {
        sqlHandler sqlHandler = new sqlHandler();
        sessionToken sT = new sessionToken();
        PreparedStatement pstmnt = conn.prepareStatement(sqlHandler.generateReports());
        pstmnt.setString(1, String.valueOf(LocalDate.now()));
        ResultSet rs = pstmnt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        int rowNum = 0;
        while (rs.next()) {
            for (int i = 1; i < columnCount + 1; i++) {
                String logString = rs.getString(i);
                System.out.println(logString + ": ");
                rowNum += 1;
            }
        }
        System.out.println(rowNum / 5);
    }
    public static void startEvent() throws SQLException {
        /*Copy and pasted from above becuase idk what else to put here*/
        sqlHandler sqlHandler = new sqlHandler();
        sessionToken sT = new sessionToken();
        PreparedStatement pstmnt = conn.prepareStatement(sqlHandler.generateReports());
        pstmnt.setString(1, String.valueOf(LocalDate.now()));
        ResultSet rs = pstmnt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        int rowNum = 0;
        while (rs.next()) {
            for (int i = 1; i < columnCount + 1; i++) {
                String logString = rs.getString(i);
                System.out.println(logString + ": ");
                rowNum += 1;
            }
        }
    }
    public static void emergency() throws SQLException {
        /*Copy and pasted from above becuase idk what else to put here*/
        sqlHandler sqlHandler = new sqlHandler();
        sessionToken sT = new sessionToken();
        PreparedStatement pstmnt = conn.prepareStatement(sqlHandler.generateReports());
        pstmnt.setString(1, String.valueOf(LocalDate.now()));
        ResultSet rs = pstmnt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        int rowNum = 0;
        while (rs.next()) {
            for (int i = 1; i < columnCount + 1; i++) {
                String logString = rs.getString(i);
                System.out.println(logString + ": ");
                rowNum += 1;
            }
        }
    }
}
