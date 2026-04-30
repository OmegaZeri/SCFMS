package com.example.dbtest;

import java.util.*;
import java.sql.*;
import java.time.*;
public class Launcher {
    static Connection conn;
    static int accessAttempt;

    public static void main(String[] args) throws SQLException {
        dbConnector();
        login();
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
    //Security officer login example: jbarrett@example.edu
    //                                'LD6i12QyMiN@'
    //login function for console
    public static void login() throws SQLException {
        Scanner scan = new Scanner(System.in);
        sessionToken sT = new sessionToken();
        System.out.println("Please enter your SCFMS email: ");
        String username = scan.nextLine();
        System.out.println("Please enter your SCFMS password: ");
        String password = scan.nextLine();
        sqlHandler sql = new sqlHandler();
        boolean resultsFound = false;
        try(PreparedStatement loginPstmnt = conn.prepareStatement(sql.loginQuery())) {
            loginPstmnt.setString(1, username);
            loginPstmnt.setString(2, password);
            try (ResultSet rs = loginPstmnt.executeQuery()) {
                accessAttempt = 0;
                if (rs.next()) {
                    resultsFound = true;
                    sT.setConsoleUsername(username);
                    sT.setConsoleUserPassword(password);
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    try(PreparedStatement phoneNumberPstmnt = conn.prepareStatement(sql.phoneNumberQuery())) {
                        phoneNumberPstmnt.setString(1, sT.getConsolePassword());
                        try (ResultSet phoneRS = phoneNumberPstmnt.executeQuery()) {
                            if (phoneRS.next()) {
                                sT.setConsolePhoneNumber(phoneRS.getLong(1));
                            }
                        }
                    }
                    try (PreparedStatement welcomePstmnt =conn.prepareStatement(sql.welcomeQuery())){
                        welcomePstmnt.setString(1, sT.getConsolePassword());
                        try (ResultSet welcomeRS = welcomePstmnt.executeQuery()){
                            if(welcomeRS.next()){
                                System.out.println("Login Successful, welcome " + welcomeRS.getString(1));
                                sT.setConsoleUserID(welcomeRS.getInt(2));
                            }
                        }
                    }
                    try (PreparedStatement permissionsPstmnt =conn.prepareStatement(sql.permsQuery())){
                    permissionsPstmnt.setString(1, sT.getConsolePassword());
                        try(ResultSet permsRS = permissionsPstmnt.executeQuery() ){
                            if (permsRS.next()){
                                if (permsRS.getInt(1) < 4) {
                                    int perms = 1;
                                    sT.setConsoleUserPermissions(perms);
                                    menu(sT.getConsolePermissions(), sT);
                                }
                                //Checks users permissions, and sets their console menu to the correct permissions
                                else if (permsRS.getInt(1) == 4) {
                                    int perms = 4;
                                    sT.setConsoleUserPermissions(perms);
                                    menu(sT.getConsolePermissions(), sT);
                                }
                            }
                        }
                    }




                    } else {
                        if (!resultsFound) {
                            accessAttempt += 1;
                            System.out.println("Invalid login info, try again.");
                            login();
                        }
                        if (accessAttempt == 3) {
                            System.out.println("You have failed to login 3 times, client will now shutdown");
                            System.exit(0);
                        }
                    }
                }
            }
        }


    public static void menu(int i, sessionToken sT) throws SQLException {
        int perms = i;
        int choice = 0;
        Scanner scan = new Scanner(System.in);
        //Undergrad Menu Choices
        if (perms == 1) {
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
        } else if (perms == 4) {
            do {
                System.out.println("------Example University Console------");
                System.out.println("      ------1. Access Logs------      ");
                System.out.println("       ------2. Start Event------     ");
                System.out.println("       ------3. Emergency------       ");
                System.out.println("   ------4. Generate Reports------       ");
                System.out.println("      ------5. Create User------       ");
                System.out.println("       ------  6. Quit   ------      ");
                choice = scan.nextInt();
                switch (choice) {
                    case 1:
                        System.out.println("Pulling your logs for the last 30 days.");
                        accessLogs(sT);
                        break;
                    case 2:
                        System.out.println("Redirecting to start event menu");
                        startEvent(sT);
                        break;
                    case 3:
                        System.out.println("Redirecting to emergency menu");
                        emergency(sT);
                        break;
                    case 4:
                        System.out.println("Generating log report");
                        genReport();
                    case 5:
                        System.out.println("Redirecting to user creation menu");
                        createUser(sT);
                    case 6:
                        System.out.println("Closing console...");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice, try again?");
                }
            } while (choice != 6);
        }
    }

    public static void accessLogs(sessionToken sT) throws SQLException {
        sqlHandler sqlHandler = new sqlHandler();
        PreparedStatement pstmnt = conn.prepareStatement(sqlHandler.logsQuery());
        pstmnt.setInt(1, sT.getConsoleUserID());
        ResultSet rsLogs = pstmnt.executeQuery();
        ResultSetMetaData rsmd = rsLogs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        boolean rowsFound = false;
        while (rsLogs.next()) {
            rowsFound = true;
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rsmd.getColumnName(i) + ": " + rsLogs.getString(i) + " ");
            }
            System.out.println();
        }
        if (!rowsFound) {
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
        menu(sT.getConsolePermissions(), sT);
    }

    public static void startEvent(sessionToken sT) throws SQLException {
        /*1. make menu for the event. 2. make user input for event. 3. implement into db? */
        Scanner scan = new Scanner(System.in);
        if (sT.getConsolePermissions() == 4) {
            System.out.println("------Start Event------");
            System.out.println("Where is the event happening?");
            String location = scan.nextLine();
            /*since this is date, could maybe connect to clock somehow?*/
            System.out.print("What is the time/ when is the event happening? (Ex: date and time)");
            String time = scan.nextLine();
            /*add event into db? */
            System.out.println("\n Event made.");
            System.out.println("Location: " + location);
            System.out.println("Time: " + time);
        }
        menu(sT.getConsolePermissions(), sT);
    }

    public static void emergency(sessionToken sT) throws SQLException {
        /*1. make emergency menu (what, where). 2. get information from DB.
         * 3. theoretically send information.  */
        Scanner scan = new Scanner(System.in);
        if (sT.getConsolePermissions() == 4) {
           System.out.println("------Emergency Report------");
           System.out.println("Where is the emergency happening?");
           String location = scan.nextLine();
           /*since this is date, could maybe connect to clock somehow?*/
           System.out.print("What is the time/ when is the emergency happening? (Ex: date and time)");
           String time = scan.nextLine();
           System.out.println("\n Emergency reported.");
           System.out.println("Location: " + location);
           System.out.println("Time: " + time);
        }
        menu(sT.getConsolePermissions(), sT);
    }
    public static void createUser(sessionToken sT) throws SQLException {
        if(twoFactorCode(sT)){
            System.out.println("User Creation Menu: " + "\nPlease pay attention to formatting");
            //Sql code use scfms;
            //select max(userID) from users;
            //insert into users (userID, userName, Email, Password, PhoneNumber, Classification, Permissions, Age, Created) values (?, ?, ?, ?, ?, ?, ?, ?, ?);
            //select "columnname" from users where "columnname" = ?
            //if (rs.next){
            //	System.out.println("columnname" entry already exists, retry)
            //}
            //Recalls menufunction
            menu(sT.getConsolePermissions(),sT);
        }
        else{
            System.out.println("Closing Console");
            System.exit(0);
        }
    }
    public static boolean twoFactorCode(sessionToken sT){
        int twofactAttempts = 0;
        Scanner phoneScan = new Scanner(System.in);
        System.out.println("Sending 2FA code to " + sT.getConsolePhoneNumber());
        Random twoFactorGenerator = new Random();
        int code = twoFactorGenerator.nextInt(90000) + 10000;
        if (code == 100000) {code -= 1;}
        System.out.println("334-670-1110: " + code );
        System.out.println("Please enter the code you received on your device.");
        boolean auth = false;
        if (twofactAttempts==3){
            System.out.println("Too many failed attempts, closing console.");
        }
        else if(phoneScan.nextInt()==code){
            System.out.println("Success! Redirecting...");
            auth = true;
        }
        else if(!auth){
            twofactAttempts =+ 1;
            System.out.println("Incorrect code, resending");
            twoFactorCode(sT);
        }
        return auth;
    }


} //end of program
