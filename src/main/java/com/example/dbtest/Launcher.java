package com.example.dbtest;
import com.example.dbtest.sqlHandler.*;
import java.util.Scanner;
import java.sql.*;

public class Launcher {
    public static void main(String[] args) throws SQLException {
        Scanner scan = new Scanner(System.in);
        sqlHandler sql = new sqlHandler();
        System.out.println("Please enter DB Credentials");
        sql.setSqlUser();
        String user = sql.getSqlUser();
        sql.setSqlPassword();
        String password = sql.getSqlPassword();
        String connection = sql.getSqlConnection();
        Connection conn = DriverManager.getConnection(connection,user,password);
        if(conn.isValid(300)) {
            System.out.println("Connection Established");
        }
        String query = "select userName, email from users where email = ?";
        PreparedStatement pstmnt = conn.prepareStatement(query);
        pstmnt.setString(1, sql.login());
        ResultSet rs = pstmnt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        while(rs.next()){
            for(int i=1; i < columnCount+1; i++){
                String userLogin= rs.getString(i);
                System.out.println(userLogin);
            }
        }
    }
}
