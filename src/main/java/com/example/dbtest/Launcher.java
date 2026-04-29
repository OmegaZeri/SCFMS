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
            pstmnt = conn.prepareStatement(sql.welcomeQuery());
            pstmnt.setString(1, password);
            ResultSet rs2 =pstmnt.executeQuery();
            ResultSetMetaData rsmd2 = rs2.getMetaData();
            columnCount = rsmd.getColumnCount();
            rs2.next();
            System.out.println("Login Successful, welcome " + rs2.getString(1));
            accessAttempt =0;
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

    public static void accessLogs() throws SQLException {
        sessionToken sT = new sessionToken();
        sqlHandler sqlHandler = new sqlHandler();
        PreparedStatement pstmnt = conn.prepareStatement(sqlHandler.logsQuery());
        //pstmnt.setString(1, sT.getConsoleUserID());
        ResultSet rs = pstmnt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        while (rs.next()) {
            for (int i = 1; i < columnCount + 1; i++) {
                String logString = rs.getString(i);
                System.out.print(logString + ": ");
            }
        }
    }
    public static void genReport() throws SQLException {
        sqlHandler sqlHandler = new sqlHandler();
        sessionToken sT = new sessionToken();
        PreparedStatement pstmnt = conn.prepareStatement(sqlHandler.reportGen());
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
}
