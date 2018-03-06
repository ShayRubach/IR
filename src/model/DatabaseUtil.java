package model;

import annotations.A;

import javax.management.Query;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class DatabaseUtil {

    public  PreparedStatement pStmt;
    public  ResultSet rs;

    public static final String ADMIN_PASS = "pwnz";
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




    @A.DBOperation
    public String[] getAvailableSourceFiles(String sourceFolderPath)  {
        ArrayList<String> docArrayList = new ArrayList<>();

        File sourceFolder = new File(sourceFolderPath);
        File[] fileList = sourceFolder.listFiles();
        for (File f : fileList ) {
            docArrayList.add(f.getName());
        }
        //convert the list to string[] and return it
        return docArrayList.toArray(new String[docArrayList.size()]);
    }

//    @A.DBOperation
//    public String[] getAvailableSourceFiles() throws SQLException {
//
//        ArrayList<String> docArrayList = new ArrayList<>();
//        StringBuilder formattedRowString = new StringBuilder();
//
//        pStmt = conn.prepareStatement(QueryUtil.GET_AVAILABLE_SOURCE_FILES);
//        rs = pStmt.executeQuery();
//
//
//        //TODO: fox logic here so it returns String[] type
//        while(rs.next()){
//            //concat all returned fields with a delimiter separating them:
//            formattedRowString.append(rs.getString(1) + DELIM);
//            formattedRowString.append(rs.getString(2) + DELIM);
//            formattedRowString.append(rs.getString(3) + DELIM);
//
//            //add the concatenated string to the arraylist:
//            docArrayList.add(formattedRowString.toString());
//
//            //flush StringBuffer:
//            formattedRowString.setLength(0);
//        }
//
//        //convert the list to string[] and return it
//        return docArrayList.toArray(new String[docArrayList.size()]);
//    }


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

    public int addFileToStorage(String fileName) throws FileNotFoundException, SQLException {
        int docId;

        //this will be the new link to the file on the local storage dir:
        String linkToFile = getLocalStoragePath() + "/" +fileName;

        //add file to db
        pStmt = conn.prepareStatement(QueryUtil.INSERT_FILE_TO_STORAGE);
        pStmt.setString(1,fileName);
        pStmt.setString(2,linkToFile);
        pStmt.setString(3,"1");     //dispay = true
        pStmt.executeUpdate();

        pStmt = conn.prepareStatement(QueryUtil.GET_DOC_ID_BY_LINK);
        pStmt.setString(1,linkToFile);
        rs = pStmt.executeQuery();
        rs.next();
        docId = rs.getInt(1);

        //move the file to the local storage
        moveFile(fileName,getSourceFilesPath(),getLocalStoragePath());

        return docId;
    }

    public void moveFile(String fileName,String src,String dst) {
        File file = new File(src + "/" + fileName);
        file.renameTo(new File(dst + "/" + fileName));
    }
}
