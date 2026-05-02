package com.example.dbtest;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.*;
import java.time.*;
import com.github.javafaker.*;
import javax.crypto.*;
public class Launcher {
    static Connection conn;
    static int accessAttempt;
    static boolean isThreat= false;
    public static void main(String[] args) throws SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        do{
            dbConnector();
            login();
        }while(monitoring(isThreat));
        System.exit(-1);
    }

    //function to connect program to database
    public static void dbConnector() throws SQLException {
        Scanner scan = new Scanner(System.in);
        sqlHandler sql = new sqlHandler();
        String user = "";
        boolean isValidUsername = false;
        System.out.println("Please enter DB Credentials");
        while(!isValidUsername){
            sql.setSqlUser();
            user = sql.getSqlUser();
            if(user != null && user.matches("^[a-zA-Z0-9]*$")){
                System.out.println("Valid input");
                isValidUsername =true;
            }
            else{
                System.out.println("Invalid Username.");
            }
        }
        sql.setSqlPassword();
        String password = sql.getSqlPassword();
        String connection = sql.getSqlConnection();
        conn = DriverManager.getConnection(connection, user, password);
        if (conn != null && conn.isValid(300)) {
            System.out.println("Connection Established");
        }
    }

    //Hard coded users:
    //Username: ctullis, Email: ctullis@example.edu, Password: securityex
    //Username: wwiesman, Email: wwiesman@example.edu, Password: undergradex
    //Username: jhutto, Email: jhutto@example.edu, Password: facultyex
    //login function for console
    public static void login() throws SQLException {
        Scanner scan = new Scanner(System.in);
        sessionToken sT = new sessionToken();
        sqlHandler sql = new sqlHandler();
        boolean resultsFound = false;
        accessAttempt = 0;
        while (accessAttempt < 3) {
            System.out.println("Please enter your SCFMS email: ");
            String username = scan.nextLine();
            System.out.println("Please enter your SCFMS password: ");
            String password = scan.nextLine();
            try (PreparedStatement loginPstmnt = conn.prepareStatement(sql.loginQuery())) {
                loginPstmnt.setString(1, username);
                loginPstmnt.setString(2, password);
                try (ResultSet rs = loginPstmnt.executeQuery()) {
                    if (rs.next()) {
                        resultsFound = true;
                        System.out.println("Login Successful");
                        sT.setConsoleUsername(username);
                        sT.setConsoleUserPassword(password);
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int columnCount = rsmd.getColumnCount();
                        try (PreparedStatement phoneNumberPstmnt = conn.prepareStatement(sql.phoneNumberQuery())) {
                            phoneNumberPstmnt.setString(1, sT.getConsolePassword());
                            try (ResultSet phoneRS = phoneNumberPstmnt.executeQuery()) {
                                if (phoneRS.next()) {
                                    sT.setConsolePhoneNumber(phoneRS.getLong(1));
                                }
                            }
                        }
                        try (PreparedStatement welcomePstmnt = conn.prepareStatement(sql.welcomeQuery())) {
                            welcomePstmnt.setString(1, sT.getConsolePassword());
                            try (ResultSet welcomeRS = welcomePstmnt.executeQuery()) {
                                if (welcomeRS.next()) {
                                    System.out.println("Login Successful, welcome " + welcomeRS.getString(1));
                                    sT.setConsoleUserID(welcomeRS.getInt(2));
                                    boolean verified = true;
                                    sT.setUserVerification(verified);
                                }
                            }
                        }
                        try (PreparedStatement permissionsPstmnt = conn.prepareStatement(sql.permsQuery())) {
                            permissionsPstmnt.setString(1, sT.getConsolePassword());
                            try (ResultSet permsRS = permissionsPstmnt.executeQuery()) {
                                if (permsRS.next()) {
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
                            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                                     IllegalBlockSizeException | BadPaddingException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    else {
                        accessAttempt ++;
                        System.out.println("Invalid login info, try again.");
                    }
                }
            }
        }
        System.out.println("You have failed to login 3 times, client will now shutdown");
        isThreat=false;
    }
    public static void menu(int i, sessionToken sT) throws SQLException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InterruptedException {
        int perms = i;
        int choice=0;
        Scanner scan = new Scanner(System.in);
        //Undergrad Menu Choices
        if (perms <4) {
            do {
                System.out.println("------Example University Console------");
                System.out.println("      ------1. Access Logs------      ");
                System.out.println("  ------1. Request Access Sim.------   ");
                System.out.println("       ------  2. Quit   ------      ");
                try {
                    choice = scan.nextInt();
                }catch(InputMismatchException e){
                    System.out.println("Invalid input");
                    menu(i, sT);
                }
                switch (choice) {
                    case 1:
                        System.out.println("Pulling your logs for the last 30 days.");
                        accessLogs(sT);
                        break;
                    case 2:
                        System.out.println("Simulating access request");
                        requestAccess(sT, sT.getConsolePermissions());
                    case 3:
                        System.out.println("Closing console...");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice, try again?");
                }
            } while (choice != 3);
        } else if (perms == 4) {
            do {
                System.out.println("------Example University Console------");
                System.out.println("      ------1. Access Logs------      ");
                System.out.println("       ------2. Start Event------     ");
                System.out.println("       ------3. Emergency------       ");
                System.out.println("    ------4. Security Alert ------    ");
                System.out.println("   ------5. Generate Reports------    ");
                System.out.println("      ------6. Create User------      ");
                System.out.println("      ------7. Guest User------       ");
                System.out.println("  ------8. Change Permissions------   ");
                System.out.println(" ------ 9.Request Access Sim. ------  ");
                System.out.println("       ------  10. Quit   ------      ");
                try {
                    choice = scan.nextInt();
                }catch(InputMismatchException e){
                    System.out.println("Invalid input");
                    menu(i, sT);
                }

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
                        System.out.println("Redirecting to security alert menu");
                    case 5:
                        System.out.println("Generating log report");
                        genReport(sT);
                        break;
                    case 6:
                        System.out.println("Redirecting to user creation menu");
                        createUser(sT);
                        break;
                    case 7:
                        System.out.println("Redirecting to guest user creation menu");
                        guestUser(sT);
                        break;
                    case 8:
                        System.out.println("Opening user editor");
                        changePermissions(sT);
                        break;
                    case 9:
                        System.out.println("Simulating access request");
                        requestAccess(sT, sT.getConsolePermissions());
                        break;
                    case 10:
                        System.out.println("Closing console...");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice, try again?");
                }
            } while (choice != 10);
        }
    }

    public static void accessLogs(sessionToken sT) throws SQLException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InterruptedException {
        if(twoFactorCode(sT)) {
            String input;
            //These lines generate the key in the DES format, and save it to key
            KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
            SecretKey key = keyGenerator.generateKey();
            //These lines initialize the cipher for encryption
            Cipher desEncryptCipher = Cipher.getInstance("DES");
            desEncryptCipher.init(Cipher.ENCRYPT_MODE, key);
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
                    input = rsmd.getColumnName(i) + ": " + rsLogs.getString(i) + " ";
                    byte[] cipherText = desEncryptCipher.doFinal(input.getBytes());
                    String logsCText = Base64.getEncoder().encodeToString(cipherText);
                    if(sT.getUserVerification()){
                        Cipher desDecryptCipher = Cipher.getInstance("DES");
                        desDecryptCipher.init(Cipher.DECRYPT_MODE, key);
                        //These lines decrypt the cypher text, first in byte form, then into text form
                        byte[] decodedBytes = Base64.getDecoder().decode(logsCText);
                        byte[] decryptedBytes = desDecryptCipher.doFinal(decodedBytes);
                        //These lines output the plain text
                        String outputPText = new String(decryptedBytes, StandardCharsets.UTF_8);
                        System.out.println(outputPText);
                    }
                    else{
                        System.out.println("User is not verified, logs are encrypted");
                        return;
                    }
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

    public static void genReport(sessionToken sT) throws SQLException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InterruptedException {
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
                int pos = 0;
                for (int i = 1; i < columnCount + 1; i++) {
                    String logString = rsmd.getColumnName(i) + ": " + rs.getString(i) + " ";
                    System.out.print(logString + " | ");
                    rowNum += 1;
                    pos ++;
                }
                if(pos %6 == 0){
                    System.out.println();
                    System.out.println();
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

    public static void startEvent(sessionToken sT) throws SQLException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InterruptedException {
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

    public static void emergency(sessionToken sT) throws SQLException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InterruptedException {
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
    public static void createUser(sessionToken sT) throws SQLException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InterruptedException {
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
    public static void guestUser(sessionToken sT) throws SQLException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InterruptedException {
        if(twoFactorCode(sT)) {
            System.out.println("Guest User Creation Menu: " + "\nPlease pay attention to formatting");
            sqlHandler sql = new sqlHandler();
            int choice =0;
            guestUserMenu(choice, sT);
            //Recalls menufunction
            menu(sT.getConsolePermissions(), sT);
        }
        else{
            System.out.println("Closing Console");
            System.exit(0);
        }
    }
    public static void createUserMenu(int choiceIn, sessionToken sT) throws SQLException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InterruptedException {
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
                        System.out.println("First name:");
                        scan.nextLine();
                        newUsernameFirst = scan.nextLine();
                        newUsernameFirst = newUsernameFirst.trim();
                        boolean inputCheck= newUsernameFirst.matches("^[a-zA-Z]+$");
                        if(inputCheck){
                            System.out.println("Valid input");
                        }
                        else{
                            System.out.println("Username cannot contain numbers or special characters, retry");
                            break;
                        }
                        System.out.println("Last name:");
                        newUsernameLast = scan.nextLine();
                        newUsernameLast = newUsernameLast.trim();
                        sT.setNewUserLast(newUsernameLast);
                        inputCheck= newUsernameLast.matches("^[a-zA-Z]+");
                        if(inputCheck){
                            System.out.println("Valid input");
                        }
                        else{
                            System.out.println("Username cannot contain numbers or special characters, retry");
                            break;
                        }
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
                    newUserPhonenumber = newUserPhoneNumberGeneration(sT, sql, newUserPhonenumber);
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
                        try{
                            classChoice=scan.nextInt();
                        }catch (InputMismatchException e){
                            System.out.println("Not a numerical choice");
                            scan.next();
                            break;
                        }
                        scan.nextLine();
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
                    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                             IllegalBlockSizeException | BadPaddingException | InterruptedException e) {
                        throw new RuntimeException(e);
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
    public static void guestUserMenu(int choiceIn, sessionToken sT) throws SQLException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InterruptedException {
        int choice = choiceIn;
        sqlHandler sql = new sqlHandler();
        int newUserID =0;
        boolean uniqueName=false;
        String newUsername = "null";
        String newUserEmail ="null";
        String newUserPassword= "null";
        String newUserPhonenumber= "null";
        int newUserAge=0;
        LocalDateTime newUserCreationDate = LocalDateTime.of(2000,1,1,0,0);
        LocalDateTime newUserRevokeDate = LocalDateTime.of(2000,1,1,0,0);
            do {
            Scanner scan = new Scanner(System.in);
            System.out.println("       ------Create User------     ");
            System.out.println("        ------1. UserID------      ");
            System.out.println("       ------2. UserName------     ");
            System.out.println("        ------3. Email ------       ");
            System.out.println("       ------4. Password------       ");
            System.out.println("     ------5. Phone number------      ");
            System.out.println("         ------6. Age ------        ");
            System.out.println("    ------ 7. Date Created ------   ");
            System.out.println("    ------ 8. Revoke Date ------   ");
            System.out.println("  ------ 9. Finish Creation ------  ");
            System.out.println(" ------ 10. Exit User Creation------ ");
            try{
                choice = scan.nextInt();
            }catch (InputMismatchException e){
                System.out.println("Not a numerical choice");
                scan.next();
                guestUserMenu(choice,sT);
            }
            boolean emailUnique=false;
            switch (choice) {
                case 1:
                    //Input UserID function
                    try (PreparedStatement userIDMaxPstmnt = conn.prepareStatement(sql.guestUserIDMaxQuery())) {
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
                    try (PreparedStatement userNamePstmnt = conn.prepareStatement(sql.guestUserNameQuery())) {
                        scan.nextLine();
                        System.out.println("First name:");
                        newUsernameFirst = scan.nextLine();
                        newUsernameFirst = newUsernameFirst.trim();
                        boolean inputCheck= newUsernameFirst.matches("^[a-zA-Z]+");
                        if(inputCheck){
                            System.out.println("Valid input");
                        }
                        else{
                            System.out.println("Username cannot contain numbers or special characters, retry");
                            break;
                        }
                        System.out.println("Last name:");
                        newUsernameLast = scan.nextLine();
                        newUsernameLast = newUsernameLast.trim();
                        sT.setNewUserLast(newUsernameLast);
                        inputCheck= newUsernameLast.matches("^[a-zA-Z]+");
                        if(inputCheck){
                            System.out.println("Valid input");
                        }
                        else{
                            System.out.println("Username cannot contain numbers or special characters, retry");
                            break;
                        }
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
                    newUserPassword = newGuestUserPasswordGeneration(sT, sql);
                    break;
                case 5:
                    //
                    System.out.println("Enter new user's phone number");
                    newUserPhonenumber = newGuestUserPhoneNumberGeneration(sT, sql, newUserPhonenumber);
                    break;
                case 6:
                    scan.nextLine();
                    newUserAge = newGuestUserAgeGetter(newUserAge);
                    break;
                case 7:
                    System.out.println("Getting creation time and date");
                    LocalDateTime created = LocalDateTime.now();
                    DateTimeFormatter sqlFormatting = DateTimeFormatter.ofPattern(("yyyy-MM-dd HH:mm:ss"));
                    newUserCreationDate = LocalDateTime.parse(created.format(sqlFormatting), sqlFormatting);
                    break;
                case 8:
                    System.out.println("Getting revoke time and date");
                    System.out.println("1. Revoke in 1 day");
                    System.out.println("2. Revoke in 1 week");
                    System.out.println("3. Revoke in 1 month");
                    int revChoice=scan.nextInt();
                    switch(revChoice){
                        case 1:
                            newUserRevokeDate = LocalDateTime.now().plusDays(1);
                            break;
                        case 2:
                            newUserRevokeDate = LocalDateTime.now().plusWeeks(1);
                            break;
                        case 3:
                            newUserRevokeDate = LocalDateTime.now().plusMonths(1);
                            break;
                        default:
                            System.out.println("Invalid choice, revoke date not set");
                    }
                    sqlFormatting = DateTimeFormatter.ofPattern(("yyyy-MM-dd HH:mm:ss"));
                    newUserRevokeDate = LocalDateTime.parse(newUserRevokeDate.format(sqlFormatting), sqlFormatting);
                    System.out.println("Guest access end date set to: " + newUserRevokeDate);
                    break;
                case 9:
                    System.out.println("Creating New User");
                    try(PreparedStatement userCreatePstmnt = conn.prepareStatement(sql.newGuestUserCreationQuery())){
                        if(newUserID==0){
                            System.out.println("User does not have an ID");
                            guestUserMenu(choice, sT);
                        }
                        else{
                            userCreatePstmnt.setInt(1,newUserID);
                            if(newUsername.equals(null)){
                                System.out.println("User does not have a name");
                                guestUserMenu(choice, sT);
                            }
                            else{
                                userCreatePstmnt.setString(2,newUsername);
                                if(newUserEmail.equals(null)){
                                    System.out.println("User does not have an email");
                                    guestUserMenu(choice, sT);
                                }
                                else{
                                    userCreatePstmnt.setString(3,newUserEmail);
                                    if(newUserPassword.equals(null)){
                                        System.out.println("User does not have a password");
                                        guestUserMenu(choice, sT);
                                    }
                                    else{
                                        userCreatePstmnt.setString(4,newUserPassword);
                                        if(newUserPhonenumber.equals(null)){
                                            System.out.println("User does not have a phone number");
                                            guestUserMenu(choice, sT);
                                        }
                                        else{
                                            userCreatePstmnt.setString(5,newUserPassword);
                                            if(newUserAge==0){
                                                System.out.println("User does not have their age set");
                                                guestUserMenu(choice, sT);
                                            }
                                            else{
                                                userCreatePstmnt.setInt(6, newUserAge);
                                                if(Objects.equals(newUserCreationDate, LocalDateTime.of(2000, 1, 1, 0, 0))){
                                                    System.out.println("User does not have their creation date set");
                                                    guestUserMenu(choice, sT);
                                                }
                                                else{
                                                    userCreatePstmnt.setString(7,String.valueOf(newUserCreationDate));
                                                    if(Objects.equals(newUserRevokeDate, LocalDateTime.of(2000, 1, 1, 0, 0))){
                                                        System.out.println("User's access end date has not been set");
                                                        guestUserMenu(choice, sT);
                                                    }
                                                    else{
                                                        userCreatePstmnt.setString(8, String.valueOf(newUserRevokeDate));
                                                    }
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
                    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                             IllegalBlockSizeException | BadPaddingException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case 11:
                    System.out.println("Closing user creator");
                    menu(sT.getConsolePermissions(), sT);
                default:
                    System.out.println("Invalid choice, try again?");
            }
        } while (choice != 10);
}
    public static void changePermissions(sessionToken sT) throws SQLException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InterruptedException {
        int permissions = permissionsGetter(sT);
        String classifications = "";
        String passwordInput = "";
        Scanner scan = new Scanner(System.in);
        sqlHandler sql = new sqlHandler();
        boolean userFound = false;
        while (!userFound){
            System.out.println("Enter user's password associated with the account you want to edit, or type exit to close");
            passwordInput = scan.nextLine();
            if (passwordInput.equalsIgnoreCase("exit")){
                permissions =4;
                menu(permissions, sT);}
            try(PreparedStatement reqPermsPstmnt = conn.prepareStatement(sql.requestPermissionsQuery())){
                reqPermsPstmnt.setString(1,passwordInput);
                try (ResultSet reqPermsRS = reqPermsPstmnt.executeQuery()){
                    if (reqPermsRS.next()){
                        System.out.println(reqPermsRS.getString(1) + "'s " + "classification is: " + reqPermsRS.getString(2) + " and their permissions are: " + reqPermsRS.getString(3));
                        userFound=true;
                    }
                    else {
                        System.out.println("User was not found with that password.");
                    }
                }
            }
        }
        if(userFound){
            if(permissions ==1){
                classifications ="undergrad";
            }
            else if (permissions==2){
                classifications ="grad";
            }
            else if(permissions==3){
                classifications = "faculty";
            }
            else if (permissions==34){
                classifications ="security officer";
            }
            else if (permissions >4 || permissions < 1){
                System.out.println("Invalid Permissions");
                return;
            }
            try(PreparedStatement changeUserPermsPstmnt = conn.prepareStatement(sql.changeUserPermissionsQuery())){
                changeUserPermsPstmnt.setString(1, classifications);
                changeUserPermsPstmnt.setInt(2, permissions);
                changeUserPermsPstmnt.setString(3, passwordInput);
                if(changeUserPermsPstmnt.executeUpdate()==1){
                    System.out.println("User's permission and classification have been updated");
                    System.out.println("User's new classification is: " + classifications + " and their permissions are: " + permissions);
                    menu(sT.getConsolePermissions(), sT);
                }
                else{
                    System.out.println("User was not updated");
                    menu(sT.getConsolePermissions(), sT);
                }
            }
        }
    }
    public static int permissionsGetter(sessionToken sT) throws SQLException{
        System.out.println("Change user permissions:");
        Scanner scan = new Scanner(System.in);
        System.out.println("Input new user's permissions");
        int newPermissions = 0;
        try {
            newPermissions = scan.nextInt();
        }
        catch(InputMismatchException e){
            System.out.println("Invalid input");
            return permissionsGetter(sT);
        }
        return newPermissions;
    }
    public static boolean monitoring(boolean isThreat)throws SQLException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if(!isThreat){
            return false;
        }
        else{return true;}
    }
    public static boolean twoFactorCode(sessionToken sT){
        int twofactAttempts = 0;
        boolean auth = false;
        Scanner phoneScan = new Scanner(System.in);
        System.out.println("Sending 2FA code to " + sT.getConsolePhoneNumber());
        Random twoFactorGenerator = new Random();
        while (twofactAttempts < 3 && !auth){
            int code = twoFactorGenerator.nextInt(90000) + 10000;
            if (code == 100000) {code -= 1;}
            int input = 0;
            System.out.println("334-670-1110: " + code );
            System.out.println("Please enter the code you received on your device.");
            try{
                input = phoneScan.nextInt();
                if(input==code) {
                    System.out.println("Success! Redirecting...");
                    auth = true;
                }
                else{
                    twofactAttempts++;
                    System.out.println("Incorrect code, resending");
                }
            }catch(InputMismatchException e){
                System.out.println("Not a numerical input, re enter");
                phoneScan.next();
                return twoFactorCode(sT);
            }
        }
        if(!auth){
            System.out.println("Too many failed attempts, closing console.");
            isThreat=false;
        }
        return auth;
    }
    public static String requestAccess(sessionToken sT, int userPermissions) throws SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {
        int roomSelection=0;
        String buildingSelection="";
        int requiredPrivilege;
        int choice=0;
        sqlHandler sql = new sqlHandler();
        boolean isAllowed=false;
        Scanner scan = new Scanner(System.in);
        while(!isAllowed){
            System.out.println("Select Building:");
            System.out.println("1. Admin Hall");
            System.out.println("2. Cedar Hall");
            System.out.println("3. Library Annex");
            System.out.println("4. North Science");
            System.out.println("5. South Science");
            System.out.println("6. Student Center");
            System.out.println("7. East Lecture Hall");
            System.out.println("8. West Lecture Hall");
            System.out.println("9. Exit");
            try{choice = scan.nextInt();}
            catch(InputMismatchException e){
                System.out.println("Invalid input, try again");
                scan.next();
                continue;
            }
            switch(choice){
                case 1:
                    buildingSelection="Admin Hall";
                    break;
                case 2:
                    buildingSelection="Cedar Hall";
                    break;
                case 3:
                    buildingSelection="Library Annex";
                    break;
                case 4:
                    buildingSelection="North Science";
                    break;
                case 5:
                    buildingSelection="South Science";
                    break;
                case 6:
                    buildingSelection="Student Center";
                    break;
                case 7:
                    buildingSelection="East Lecture Hall";
                    break;
                case 8:
                    buildingSelection="West Lecture Hall";
                    break;
                case 9:
                    System.out.println("Exiting ");
                    return null;
                default: System.out.println("Invalid Choice"); continue;
            }
            handleBuildingSelection(buildingSelection, sT, sql, scan);
            isAllowed=true;


        }
        return null;
    }
    public static void roomOpen(int roomIDInput, sqlHandler sql)throws SQLException, InterruptedException{
        try(PreparedStatement roomUnlockPstmnt = conn.prepareStatement(sql.roomUnlockUpdate())) {
            roomUnlockPstmnt.setInt(1, roomIDInput);
            if(roomUnlockPstmnt.executeUpdate()==1){
                System.out.println("Room unlocked");
                Thread.sleep(10000);
                try(PreparedStatement roomLockPstmnt = conn.prepareStatement(sql.roomLockUpdate())){
                    roomLockPstmnt.setInt(1, roomIDInput);
                    if(roomLockPstmnt.executeUpdate()==1){
                        System.out.println("Room now locked");
                    }
                    else{
                        System.out.println("Room may be overridden");
                    }
                }
            }
            else{
                System.out.println("Room may be overridden");
            }
        }
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
    public static String newGuestUserPasswordGeneration (sessionToken sT, sqlHandler sqlIn)throws SQLException {
        System.out.println("Password created");
        Faker passwordFaker = new Faker();
        boolean passwordGenerated=false;
        String newUserPassword = passwordFaker.internet().password(12,13,true,true,true);
        newUserPassword = newUserPassword.substring(1);
        try(PreparedStatement passwordPstmnt = conn.prepareStatement(sqlIn.guestUserPasswordQuery())){
            passwordPstmnt.setString(1,newUserPassword);
            try(ResultSet passwordRS = passwordPstmnt.executeQuery()){
                if(passwordRS.next()){
                    System.out.println("Password already exists, retrying");
                    newGuestUserPasswordGeneration(sT,sqlIn);
                }
                else{passwordGenerated = true;}
            }
        }
        if(passwordGenerated){
            return newUserPassword;
        }
        else{return null;}
    }
    public static String newUserPhoneNumberGeneration (sessionToken sT, sqlHandler sqlIn, String i) throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter a phone number:");
        System.out.println("Format xxxyyyzzzz");
        String newUserPhoneNumber = scan.nextLine();
        if (newUserPhoneNumber.length() != 10 || !newUserPhoneNumber.matches("[0-9]+")) {
            System.out.println("Invalid format, try again");
            return newUserPhoneNumberGeneration(sT, sqlIn, newUserPhoneNumber);
        }
        try (PreparedStatement phoneNumberPstmnt = conn.prepareStatement(sqlIn.userPhoneNumberQuery())) {
            phoneNumberPstmnt.setString(1, newUserPhoneNumber);
            try (ResultSet phoneRS = phoneNumberPstmnt.executeQuery()) {
                if (phoneRS.next()) {
                    System.out.println("Phone number exists, enter a new one");
                    return newUserPhoneNumberGeneration(sT, sqlIn, newUserPhoneNumber);
                } else {
                    System.out.println("Phone number accepted");
                    return newUserPhoneNumber;
                }
            }

        }catch(SQLException e){
            System.out.println("Database error occurred, restarting");
            return newUserPhoneNumberGeneration(sT, sqlIn, newUserPhoneNumber);
        }
    }
    public static String newGuestUserPhoneNumberGeneration (sessionToken sT, sqlHandler sqlIn, String i) throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter a phone number:");
        System.out.println("Format xxxyyyzzzz");
        String newGuestUserPhoneNumber = scan.nextLine();
        String input = i;
        if (newGuestUserPhoneNumber.length() != 10 || !newGuestUserPhoneNumber.matches("[0-9]+")) {
            System.out.println("Invalid format, try again");
            return newUserPhoneNumberGeneration(sT, sqlIn, newGuestUserPhoneNumber);
        }
        try (PreparedStatement phoneNumberPstmnt = conn.prepareStatement(sqlIn.guestPhoneNumberQuery())) {
            phoneNumberPstmnt.setString(1, newGuestUserPhoneNumber);
            try (ResultSet phoneRS = phoneNumberPstmnt.executeQuery()) {
                if (phoneRS.next()) {
                    System.out.println("Phone number exists, enter a new one");
                    return newGuestUserPhoneNumberGeneration(sT, sqlIn, newGuestUserPhoneNumber);
                }
                else {
                    System.out.println("Phone number accepted");
                    return newGuestUserPhoneNumber;
                }
            }

        }catch(SQLException e){
            System.out.println("Database error occurred, restarting");
            return newGuestUserPhoneNumberGeneration(sT, sqlIn, newGuestUserPhoneNumber);
        }
    }
    public static int newUserAgeGetter(int newUserAge){
        System.out.println("Enter new user's age");
        Scanner scan = new Scanner(System.in);
        try{
            newUserAge = scan.nextInt();
        }catch(InputMismatchException e){
            System.out.println("Not a numerical input, re enter");
            scan.nextLine();
            return newUserAgeGetter(newUserAge);
        }
        if(newUserAge > 99 || newUserAge < 1) {System.out.println("There's no way, re enter age");
            newUserAge = 0;
        }
        return newUserAge;
    }
    public static int newGuestUserAgeGetter(int newUserAge){
        System.out.println("Enter new user's age");
        Scanner scan = new Scanner(System.in);
        try{
            newUserAge = scan.nextInt();
        }catch(InputMismatchException e){
            System.out.println("Not a numerical input, re enter");
            scan.next();
            newGuestUserAgeGetter(newUserAge);
        }
        if(newUserAge > 99 || newUserAge < 1) {System.out.println("There's no way, re enter age");
            newUserAge = 0;
        }
        return newUserAge;
    }
    private static void handleBuildingSelection(String buildingSelection, sessionToken sT, sqlHandler sql, Scanner scan) throws SQLException, InterruptedException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        try(PreparedStatement roomQueryPstmnt = conn.prepareStatement(sql.roomQuery())){
            String input = "%" + buildingSelection + "%";
            roomQueryPstmnt.setString(1, input);
            try(ResultSet roomQueryRS = roomQueryPstmnt.executeQuery()){
                ResultSetMetaData rsmd = roomQueryRS.getMetaData();
                int columnCount = rsmd.getColumnCount();
                boolean rowsFound = false;
                System.out.println("User's permissions: " + sT.getConsolePermissions());
                while(roomQueryRS.next()){
                    rowsFound = true;
                    for(int i = 1; i <= columnCount; i++){
                        System.out.println(i + ": " + rsmd.getColumnName(i) + ": " + roomQueryRS.getInt(i));
                    }
                }
                if(rowsFound){
                    System.out.println("Select a roomID");
                    try{
                        int roomSelection = scan.nextInt();
                        try(PreparedStatement roomExistsPstmnt = conn.prepareStatement(sql.roomExistsQuery())){
                            roomExistsPstmnt.setInt(1, roomSelection);
                            try(ResultSet roomExistsRS = roomExistsPstmnt.executeQuery()){
                                if(roomExistsRS.next() && roomExistsRS.getInt(1) <= roomSelection){
                                    roomOpen(roomSelection, sql);
                                }
                                else{
                                    System.out.println("That room does not exist");
                                }
                            }
                        }
                    } catch(InputMismatchException e){
                        System.out.println("Invalid input");
                        scan.next();
                    }
                } else {
                    System.out.println("No rooms found in that building");
                }
            }
        }
    }
} //end of program
