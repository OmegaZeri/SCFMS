package com.example.dbtest;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.*;
import java.time.*;
import com.github.javafaker.*;
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

    //Undergrad login example:    ctullis@example.edu
    //                            a@@d*fWYJD^
    //Security officer login example: jbarrett@example.edu
    //                                LD6i12QyMiN@
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
        int choice;
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
                        genReport(sT);
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
        if(twoFactorCode(sT)) {
            System.out.println("Accessing... " + "\nPlease pay attention to formatting");
            sqlHandler sql = new sqlHandler();
            int choice =0;
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
            //Recalls menufunction
            menu(sT.getConsolePermissions(), sT);
        }
        else{
            System.out.println("Closing Console");
            System.exit(0);
        }
    }

    public static void genReport(sessionToken sT) throws SQLException {
        if(twoFactorCode(sT)) {
            System.out.println("Report Menu: " + "\nPlease pay attention to formatting");
            sqlHandler sql = new sqlHandler();
            int choice =0;
            sqlHandler sqlHandler = new sqlHandler();
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
            //Recalls menufunction
            menu(sT.getConsolePermissions(), sT);
        }
        else{
            System.out.println("Closing Console");
            System.exit(0);
        }
    }

    public static void startEvent(sessionToken sT) throws SQLException {
        if(twoFactorCode(sT)) {
            /*1. make menu for the event. 2. make user input for event. 3. implement into db? */
            System.out.println("Event Menu: " + "\nPlease pay attention to formatting");
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
            //Recalls menufunction
            menu(sT.getConsolePermissions(), sT);
        }
        else{
            System.out.println("Closing Console");
            System.exit(0);
        }

    }

    public static void emergency(sessionToken sT) throws SQLException {
        /*1. make emergency menu (what, where). 2. get information from DB.
         * 3. theoretically send information.  */
        if(twoFactorCode(sT)) {
            System.out.println("Emergency Menu: " + "\nPlease pay attention to formatting");
            sqlHandler sql = new sqlHandler();
            int choice =0;
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
            //Recalls menufunction
            menu(sT.getConsolePermissions(), sT);
        }
        else{
            System.out.println("Closing Console");
            System.exit(0);
        }
    }
    public static void createUser(sessionToken sT) throws SQLException {
        if(twoFactorCode(sT)) {
            System.out.println("User Creation Menu: " + "\nPlease pay attention to formatting");
            sqlHandler sql = new sqlHandler();
            int choice =0;
            createUserMenu(choice, sT);
            //Recalls menufunction
            menu(sT.getConsolePermissions(), sT);
        }
        else{
            System.out.println("Closing Console");
            System.exit(0);
        }
    }
    public static void createUserMenu(int choiceIn, sessionToken sT)throws SQLException{
        int choice = choiceIn;
        sqlHandler sql = new sqlHandler();
        int newUserID =0;
        boolean uniqueName=false;
        String newUsername = "null";
        String newUserEmail ="null";
        String newUserPassword= "null";
        String newUserPhonenumber= "null";
        String newUserClassification="null";
        int newUserPermissions=0;
        int newUserAge=0;
        LocalDateTime newUserCreationDate = LocalDateTime.of(2000,1,1,0,0);
        do {
            Scanner scan = new Scanner(System.in);
            System.out.println("       ------Create User------     ");
            System.out.println("        ------1. UserID------      ");
            System.out.println("       ------2. UserName------     ");
            System.out.println("        ------3. Email ------       ");
            System.out.println("       ------4. Password------       ");
            System.out.println("     ------5. Phone number------      ");
            System.out.println("   ------ 6. Classification ------  ");
            System.out.println("    ------  7. Permissions ------   ");
            System.out.println("         ------8. Age ------        ");
            System.out.println("    ------ 9. Date Created ------   ");
            System.out.println("  ------ 10. Finish Creation ------  ");
            System.out.println(" ------ 11. Exit User Creation------ ");
            try{
                choice = scan.nextInt();
            }catch (InputMismatchException e){
                System.out.println("Not a numerical choice");
                scan.next();
                createUserMenu(choice,sT);
            }
            boolean emailUnique=false;
            switch (choice) {
                case 1:
                    //Input UserID function
                    try (PreparedStatement userIDMaxPstmnt = conn.prepareStatement(sql.userIDMaxQuery())) {
                        try (ResultSet userIDMaxRS = userIDMaxPstmnt.executeQuery()) {
                            if (userIDMaxRS.next()) {
                                int maxStudentID = userIDMaxRS.getInt(1);
                                System.out.println("The largest userID currently existing is: " + maxStudentID);
                                System.out.println("Entering " + (maxStudentID + 1) + " into new user's ID field");
                                newUserID = (maxStudentID+1);
                            }
                        }
                    }
                    break;
                case 2:
                    //Input Username function
                    System.out.println("Input name of user: ");
                    String newUsernameFirst, newUsernameLast;
                    try (PreparedStatement userNamePstmnt = conn.prepareStatement(sql.userNameQuery())) {
                        scan.nextLine();
                        System.out.println("First name:");
                        newUsernameFirst = scan.nextLine();
                        System.out.println("Last name:");
                        newUsernameLast = scan.nextLine();
                        sT.setNewUserLast(newUsernameLast);
                        newUsername = String.join(" ", newUsernameFirst, newUsernameLast);
                        sT.setNewUsername(newUsername);
                        userNamePstmnt.setString(1, newUsername);
                        try (ResultSet userNameRS = userNamePstmnt.executeQuery()) {
                            if (!userNameRS.next()) {
                                System.out.println("There does not exist a user with that name, email will be unique.");
                                uniqueName=true;
                            }
                            else {
                                System.out.println("There exists a user with that name, email will have to contain unique numbers.");
                            }
                        }
                    }
                    break;
                case 3:
                    //Email creation function
                    if(sT.getNewUsername()==null){
                        System.out.println("Enter Username first before creating user email.");
                        break;
                    }
                    else{
                        System.out.println("Email creation:");
                        String firstLetterFirstName = String.valueOf(sT.getNewUsername().toLowerCase().charAt(0));
                        String fullLastName = sT.getNewUsernameLast().toLowerCase();
                        if(uniqueName){
                            newUserEmail = firstLetterFirstName + fullLastName + "@example.edu";
                            System.out.println("Email to be inputted is: " + newUserEmail);
                        }
                        else{
                            Random emailRand = new Random();
                            int emailNum = emailRand.nextInt(130000);
                            if (emailNum < 100000) {emailNum += 100000;}
                            String emailNumString = String.valueOf(emailNum);
                            newUserEmail = firstLetterFirstName + fullLastName + emailNumString + "@example.edu";
                            System.out.println("Email to be inputted is: " + newUserEmail);
                        }
                    }
                    break;
                case 4:
                    //Input password function
                    newUserPassword = newUserPasswordGeneration(sT, sql);
                    break;
                case 5:
                    //
                    System.out.println("Enter new user's phone number");
                    newUserPhonenumber = newUserPhoneNumberGeneration(sT, sql);
                    break;
                case 6:
                    int classChoice;
                    do{
                        System.out.println("""
                            Enter user's classification
                            1.Undergraduate Student
                            2.Graduate Student
                            3.Faculty
                            4.Security Officer
                            5.Close menu
                            """);
                        classChoice=scan.nextInt();
                        scan.nextLine();
                        String classificationChoice;
                        switch(classChoice){
                            case 1:
                                System.out.println("User is an undergraduate student");
                                newUserClassification = "undergrad";
                                classChoice=5;
                                break;
                            case 2:
                                System.out.println("User is an graduate student");
                                newUserClassification = "grad";
                                classChoice=5;
                                break;
                            case 3:
                                System.out.println("User is a faculty member");
                                newUserClassification = "faculty";
                                classChoice=5;
                                break;
                            case 4:
                                System.out.println("User is a security officer");
                                newUserClassification = "security officer";
                                classChoice=5;
                                break;
                            case 5:
                                System.out.println("Closing menu");
                                break;
                            default:
                                System.out.println("Invalid choice, try again?");
                        }
                    }while (classChoice !=5);
                    break;
                case 7:
                    System.out.println("Permissions Assigned");
                        if(newUserClassification.equals("undergrad")){
                            newUserPermissions = 1;
                        }
                        else if(newUserClassification.equals("grad")){
                            newUserPermissions = 2;
                        }
                        else if(newUserClassification.equals("faculty")){
                            newUserPermissions = 3;
                        }
                        else if(newUserClassification.equals("security officer")){
                            newUserPermissions = 4;
                        }
                    break;
                case 8:
                    scan.nextLine();
                    newUserAge = newUserAgeGetter(newUserAge);
                    break;
                case 9:
                    System.out.println("Getting creation time and date");
                    LocalDateTime created = LocalDateTime.now();
                    DateTimeFormatter sqlFormatting = DateTimeFormatter.ofPattern(("yyyy-MM-dd HH:mm:ss"));
                    newUserCreationDate = LocalDateTime.parse(created.format(sqlFormatting), sqlFormatting);
                    break;
                case 10:
                    System.out.println("Creating New User");
                    try(PreparedStatement userCreatePstmnt = conn.prepareStatement(sql.newUserCreationQuery())){
                        if(newUserID==0){
                            System.out.println("User does not have an ID");
                            createUserMenu(choice, sT);
                        }
                        else{
                            userCreatePstmnt.setInt(1,newUserID);
                            if(newUsername.equals(null)){
                                System.out.println("User does not have a name");
                                createUserMenu(choice, sT);
                            }
                            else{
                                userCreatePstmnt.setString(2,newUsername);
                                if(newUserEmail.equals(null)){
                                    System.out.println("User does not have an email");
                                    createUserMenu(choice, sT);
                                }
                                else{
                                    userCreatePstmnt.setString(3,newUserEmail);
                                    if(newUserPassword.equals(null)){
                                        System.out.println("User does not have a password");
                                        createUserMenu(choice, sT);
                                    }
                                    else{
                                        userCreatePstmnt.setString(4,newUserPassword);
                                        if(newUserPhonenumber.equals(null)){
                                            System.out.println("User does not have a phone number");
                                            createUserMenu(choice, sT);
                                        }
                                        else{
                                            userCreatePstmnt.setString(5,newUserPhonenumber);
                                            if(newUserClassification.equals(null)){
                                                System.out.println("User does not have their classification");
                                                createUserMenu(choice, sT);
                                            }
                                            else{
                                                userCreatePstmnt.setString(6, newUserClassification);
                                                if(newUserPermissions==0){
                                                    System.out.println("User does not have their permissions set");
                                                    createUserMenu(choice, sT);
                                                }
                                                else{
                                                    userCreatePstmnt.setInt(7,newUserPermissions);
                                                    if(newUserAge==0){
                                                        System.out.println("User does not have their age set");
                                                        createUserMenu(choice, sT);
                                                    }
                                                    else{
                                                        userCreatePstmnt.setInt(8, newUserAge);
                                                        if(Objects.equals(newUserCreationDate, LocalDateTime.of(2000, 1, 1, 0, 0))){
                                                            System.out.println("User does not have their creation date set");
                                                            createUserMenu(choice, sT);
                                                        }
                                                        else{
                                                            userCreatePstmnt.setString(9,String.valueOf(newUserCreationDate));
                                                            System.out.println("User info has been accepted, creating...");
                                                            boolean userCreated=false;
                                                            if(userCreatePstmnt.executeUpdate()==1){
                                                                userCreated=true;
                                                                System.out.println("User has been created");
                                                                menu(sT.getConsolePermissions(), sT);
                                                            }
                                                            else{
                                                                System.out.println("User was not created");
                                                                menu(sT.getConsolePermissions(), sT);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    break;
                case 11:
                    System.out.println("Closing user creator");
                    menu(sT.getConsolePermissions(), sT);
                default:
                    System.out.println("Invalid choice, try again?");
            }
        } while (choice != 11);
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
    public static String newUserPasswordGeneration (sessionToken sT, sqlHandler sqlIn)throws SQLException {
        System.out.println("Password created");
        Faker passwordFaker = new Faker();
        boolean passwordGenerated=false;
        String newUserPassword = passwordFaker.internet().password(12,13,true,true,true);
        newUserPassword = newUserPassword.substring(1);
        try(PreparedStatement passwordPstmnt = conn.prepareStatement(sqlIn.userPasswordQuery())){
            passwordPstmnt.setString(1,newUserPassword);
            try(ResultSet passwordRS = passwordPstmnt.executeQuery()){
                if(passwordRS.next()){
                    System.out.println("Password already exists, retrying");
                    newUserPasswordGeneration(sT,sqlIn);
                }
                else{passwordGenerated = true;}
            }
        }
        if(passwordGenerated){
            return newUserPassword;
        }
        else{return null;}
    }
    public static String newUserPhoneNumberGeneration (sessionToken sT, sqlHandler sqlIn) throws SQLException {
        System.out.println("Phone Number Applied");
        Faker phoneFaker = new Faker();
        boolean phoneNumberGenerated = false;
        String newUserPhoneNumber = phoneFaker.phoneNumber().phoneNumber();
        try (PreparedStatement phoneNumberPstmnt = conn.prepareStatement(sqlIn.userPhoneNumberQuery())) {
            phoneNumberPstmnt.setString(1, newUserPhoneNumber);
            try (ResultSet phoneRS = phoneNumberPstmnt.executeQuery()) {
                if (phoneRS.next()) {
                    newUserPhoneNumberGeneration(sT, sqlIn);
                }
                else{ phoneNumberGenerated=true;}
            }

        }
        if(phoneNumberGenerated){
            return newUserPhoneNumber;
        }
        else{return null;}
    }
    public static int newUserAgeGetter(int newUserAge){
        System.out.println("Enter new user's age");
        Scanner scan = new Scanner(System.in);
        newUserAge = scan.nextInt();
        scan.nextLine();
        if(newUserAge > 99 && newUserAge < 1) {System.out.println("There's no way, re enter age");
            newUserAge = 0;
        }
        return newUserAge;
    }
} //end of program
