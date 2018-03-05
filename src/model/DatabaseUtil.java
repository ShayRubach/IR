package model;

import annotations.A;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseUtil {

    public  PreparedStatement pStmt;
    public  ResultSet rs;

    public static final String ADMIN_PASS = "ir_master";
    public static final String HIDE = "0";
    public static final String SHOW = "1";
    public static final String DELIM = "^";


    private String localStoragePath;
    private String sourceFilesPath;


    private String user = "root";
    private String pass = "";
    private String dbDriver = "com.mysql.jdbc.Driver";
    private String dbConfig = "jdbc:mysql://localhost:3306/IR?createDatabaseIfNotExist=true";
    private Connection conn;
    private boolean adminAccess = false;


    public DatabaseUtil(String sourceFilesPath, String localStoragePath) {
        setSourceFilesPath(sourceFilesPath);
        setLocalStoragePath(localStoragePath);
    }

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName(getDbDriver());
        conn = DriverManager.getConnection(getDbConfig(), getUser(), getPass());
    }

    public void createTable(String query) throws SQLException {
        pStmt = conn.prepareStatement(query);
        pStmt.execute();
    }

    @A.Posting
    //tag a SINGLE source files and upload it to "source_files" table
    public void tagSourceFile(File f) throws SQLException {


        String fullFilePath = f.getAbsolutePath().toString();
        String fixedFileName = f.getName().substring(0,f.getName().indexOf('.')).toString();

        //make sure f is a single file and not a dir
        if(f.isDirectory())
            return;

        //tag f to db as long as its not already there
        if(!fileAlreadyTagged(fixedFileName,fullFilePath)) {
            pStmt = conn.prepareStatement(QueryUtil.INSERT_SOURCE_FILE);
            pStmt.setString(1,fixedFileName);
            pStmt.setString(2,fullFilePath);
            pStmt.setString(3,SHOW);
            pStmt.execute();
        }
    }

    @A.Posting
    //tag all source files and upload them to "source_files" table
    public void tagAllSourceFiles() throws SQLException {

        //get ALL source files from 'source_files' folder (mimics the internet)
        File files = new File(getSourceFilesPath());
        File[] allFiles = files.listFiles();

        //tag all files from 'source_files' folder
        for(File f : allFiles )
            tagSourceFile(f);
    }

    //check if a file is already tagged into DB
    private boolean fileAlreadyTagged(String name,String path) throws SQLException {
        boolean isAlreadyTagged = true;
        pStmt = conn.prepareStatement(QueryUtil.IS_SOURCE_FILE_EXISTS);
        pStmt.setString(1,name);
        pStmt.setString(2,path);
        rs = pStmt.executeQuery();

        //as long as count returns as zero, means we don't have this record on our db.
        while(rs.next()){
            if(rs.getString(1).toString().equals("0")){
                isAlreadyTagged = false;
            }
        }

        return isAlreadyTagged;
    }


    @A.DBOperation
    public String[] getAvailableSourceFiles() throws SQLException {

        ArrayList<String> docArrayList = new ArrayList<>();
        StringBuilder formattedRowString = new StringBuilder();

        pStmt = conn.prepareStatement(QueryUtil.GET_AVAILABLE_SOURCE_FILES);
        rs = pStmt.executeQuery();


        //TODO: fox logic here so it returns String[] type
        while(rs.next()){
            //concat all returned fields with a delimiter separating them:
            formattedRowString.append(rs.getString(1) + DELIM);
            formattedRowString.append(rs.getString(2) + DELIM);
            formattedRowString.append(rs.getString(3) + DELIM);

            //add the concatenated string to the arraylist:
            docArrayList.add(formattedRowString.toString());

            //flush StringBuffer:
            formattedRowString.setLength(0);
        }

        //convert the list to string[] and return it
        return docArrayList.toArray(new String[docArrayList.size()]);
    }


    public boolean verifyAdminPass(String adminPass){
        return ADMIN_PASS.equals(adminPass);
    }

    public void setAdminAccess(boolean adminAccess) {
        this.adminAccess = adminAccess;
    }

    public boolean isLoggedAsAdmin(){
        return adminAccess;
    }













    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public String getDbConfig() {
        return dbConfig;
    }

    public String getLocalStoragePath() {
        return localStoragePath;
    }

    public void setLocalStoragePath(String localStoragePath) {
        this.localStoragePath = localStoragePath;
    }

    public String getSourceFilesPath() {
        return sourceFilesPath;
    }

    public void setSourceFilesPath(String sourceFilesPath) {
        this.sourceFilesPath = sourceFilesPath;
    }
}
