package com.example.dbtest;
import java.util.Scanner;
public class sqlHandler {
                private String sqlConnection = "jdbc:mysql://localhost:3306/SCFMS";
                private String sqlUser;
                private String sqlPassword;
                public Scanner scan = new Scanner(System.in);
                public String getSqlConnection() {
                    return sqlConnection;
                }

                public String getSqlUser() {
                    return sqlUser;
                }

                public String getSqlPassword() {
                    return sqlPassword;
                }
                public void setSqlUser() {
                    System.out.println("Please enter your username: ");
                    sqlUser = scan.nextLine();
                    this.sqlUser = sqlUser;
                }
                public void setSqlPassword() {
                    System.out.println("Please enter your password: ");
                    sqlPassword = scan.nextLine();
                    this.sqlPassword = sqlPassword;
                }
                public String login(){
                    System.out.println("Please enter your SCFMS email:");
                    return scan.nextLine();
                }
}
