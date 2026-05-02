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
                public String logsQuery (){
                    return "select username, users.userID, logs.roomID, logDate, rooms.BuildingID, buildingName from users inner join logs on users.userID = logs.userID inner join rooms on logs.roomID = rooms.roomID inner join buildings on rooms.buildingID = buildings.buildingID where logDate >= date_sub(curdate(), interval 30 day) and users.userID = ?";
                }
                public String loginQuery () {return "select email, password from users where email = ? AND password = ?";}
                public String welcomeQuery() {return "select userName, userID from users where password = ?";}
                public String generateReports (){
                    return "select username, users.userID, logs.roomID, logDate, rooms.BuildingID, buildingName from users inner join logs on users.userID = logs.userID inner join rooms on logs.roomID = rooms.roomID inner join buildings on rooms.buildingID = buildings.buildingID where logDate >= ? - interval 30 day";
                }
                public String permsQuery(){
                    return "select permissions from users where password = ?";
                }
                public String phoneNumberQuery(){return "select phoneNumber from users where password = ?";}
                public String userIDMaxQuery(){return "select max(userID) from users";}
                public String guestUserIDMaxQuery(){return "select max(userID) from guest_users";}
                public String userNameQuery(){return "select userID from users where userID =? order by userID desc limit 1";}
                public String guestUserNameQuery(){return "select userID from guest_users where userID =? order by userID desc limit 1";}
                public String userPasswordQuery(){return "select password from users where password = ?";}
                public String guestUserPasswordQuery(){return "select password from guest_users where password = ?";}
                public String userPhoneNumberQuery(){return "select phonenumber from users where phonenumber = ?";}
                public String guestPhoneNumberQuery(){return "select phoneNumber from guest_users where phonenumber = ?";}
                public String newUserCreationQuery(){return "insert into users (userID, userName, Email, Password, PhoneNumber, Classification, Permissions, Age, Created) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";}
                public String newGuestUserCreationQuery(){return "insert into guest_users (userID, userName, Email, Password, PhoneNumber, Age, Created, revokeAccess) values (?, ?, ?, ?, ?, ?, ?, ?)";}
                public String buildingListQuery(){return "select buildingID, buildingName from buildings order by buildingID";}
                public String floorListQuery(){return "select distinct floor((roomID % 1000) / 100) as floorNumber from rooms where buildingID = ? order by floorNumber";}
                public String roomsByFloorQuery(){return "select roomID from rooms where buildingID = ? and floor((roomID % 1000) / 100) = ? order by roomID";}
                public String newEventLogQuery(){return "insert into EventLog (userID, buildingID, roomID, EventType, Description, eventDate, Created) values (?, ?, ?, ?, ?, ?, ?)";}
                public String requestPermissionsQuery(){return "select username, classification, permissions from users where password = ?";}
                public String changeUserPermissionsQuery(){return "update users set classification = ?, permissions = ? where password = ?";}
                public String roomQuery(){return "select roomID, privilegerequired from rooms natural join buildings where buildings.buildingname like ?";}
                public String roomExistsQuery(){return"select privilegerequired from rooms where roomID = ?";}
                public String roomUnlockUpdate(){return "update rooms set islocked = 0 where roomID=?";}
                public String roomLockUpdate(){return "update rooms set islocked = 1 where roomID=?";}
}
