package com.example.dbtest;


import java.sql.*;

public class Launcher {
    public static void main(String[] args) throws SQLException {
        String connection = "jdbc:mysql://localhost:3306/SCFMS";
        String user  = "root";
        String password = "T1lting@W1ndm1lls*";
        Connection conn = DriverManager.getConnection(connection,user,password);
        Statement stmnt = conn.createStatement();
        ResultSet rs = stmnt.executeQuery("select userName from users where userID >= 1040000");
        while(rs.next()){
            String databaseName= rs.getString(1);
            System.out.println(databaseName);
        }
    }
}
