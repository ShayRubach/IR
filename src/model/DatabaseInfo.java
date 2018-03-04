package model;

import java.io.File;
import java.sql.*;

public class DatabaseInfo {

    private String localStoragePath;
    private String sourceFilesPath;

    private String user = "root";
    private String pass = "";
    private String dbDriver = "com.mysql.jdbc.Driver";
    private String dbConfig = "jdbc:mysql://localhost:3306/IR?createDatabaseIfNotExist=true";
    private Connection conn;
    public  PreparedStatement pStmt;
    public  ResultSet rs;







    public DatabaseInfo() {}

    public DatabaseInfo(String sourceFilesPath, String localStoragePath) {
        setSourceFilesPath(sourceFilesPath);
        setLocalStoragePath(localStoragePath);
    }

    public DatabaseInfo(String user, String pass, String dbDriver, String dbConfig) {
        this.user = user;
        this.pass = pass;
        this.dbDriver = dbDriver;
        this.dbConfig = dbConfig;
    }

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName(getDbDriver());
        conn = DriverManager.getConnection(getDbConfig(), getUser(), getPass());
    }

    public void createTable(String query) throws SQLException {
        pStmt = conn.prepareStatement(query);
        pStmt.execute();
    }

    //tag a SINGLE source files and upload it to "source_files" table
    public void tagSourceFile(File f) throws SQLException {

        String fullFilePath = f.getAbsolutePath().toString();
        String fixedFileName = f.getName().substring(0,f.getName().indexOf('.')).toString();

        //make sure f is a single file and not a dir
        if(f.isDirectory())
            return;

        //tag f to db as long as its not already there
        if(!fileAlreadyTagged(fixedFileName,fullFilePath)) {
            pStmt = conn.prepareStatement(QueryHolder.INSERT_SOURCE_FILE);
            pStmt.setString(1,fixedFileName);
            pStmt.setString(2,fullFilePath);
            pStmt.execute();
        }
    }

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
        pStmt = conn.prepareStatement(QueryHolder.IS_SOURCE_FILE_EXISTS);
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
