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
                    System.out.println("Please enter your DB username: ");
                    sqlUser = scan.nextLine();
                }
                public void setSqlPassword() {
                    System.out.println("Please enter your DB password: ");
                    sqlPassword = scan.nextLine();
                }
                public String logQuery (){
                    return "select userID, logs.roomID, logDate, rooms.BuildingID, buildingName from logs inner join rooms on logs.roomID = rooms.roomID inner join buildings on rooms.buildingID = buildings.buildingID where userID = 1003372";
                }
                public String reportGen (){
                    return "select userID, logs.roomID, logDate, rooms.BuildingID, buildingName from logs inner join rooms on logs.roomID = rooms.roomID inner join buildings on rooms.buildingID = buildings.buildingID where logDate >= ? - interval 30 day";
                }

}
