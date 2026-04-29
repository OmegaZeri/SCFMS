package com.example.dbtest;
import com.example.dbtest.sqlHandler.*;
import com.example.dbtest.sessionToken.*;
import java.util.Scanner;
import java.sql.*;
import java.time.*;
public class Launcher {
    static Connection conn;

    public static void main(String[] args) throws SQLException {
        dbConnector();
        //login();
        accessLogs();
        genReport();
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

    //login function for console
    public static void login() throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Please enter your SCFMS username: ");
        String username = scan.nextLine();
        System.out.println("Please enter your SCFMS password: ");
        String password = scan.nextLine();
        sqlHandler sql = new sqlHandler();
        String query = "select userName, password from users where userName = ?, AND password = ?";
        PreparedStatement pstmnt = conn.prepareStatement(query);
        pstmnt.setString(1, username);
        pstmnt.setString(2, password);
        ResultSet rs = pstmnt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        while (rs.next()) {
            for (int i = 1; i < columnCount + 1; i++) {
                String userLogin = rs.getString(i);
                System.out.println(userLogin);
            }
        }
    }

    public static void accessLogs() throws SQLException {
        sessionToken sT = new sessionToken();
        sqlHandler sqlHandler = new sqlHandler();
        PreparedStatement pstmnt = conn.prepareStatement(sqlHandler.logQuery());
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
