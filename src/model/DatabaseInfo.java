package model;

import java.sql.*;

public class DatabaseInfo {

    private String user = "root";
    private String pass = "";
    private String dbDriver = "com.mysql.jdbc.Driver";
    private String dbConfig = "jdbc:mysql://localhost:3306/IR?createDatabaseIfNotExist=true";

    private Connection conn;
    public  PreparedStatement pStmt;
    public  ResultSet rs;

    public DatabaseInfo() {}

    public DatabaseInfo(String user, String pass, String dbDriver, String dbConfig) {
        this.user = user;
        this.pass = pass;
        this.dbDriver = dbDriver;
        this.dbConfig = dbConfig;
    }


    public void
    connect() throws ClassNotFoundException, SQLException {
        Class.forName(getDbDriver());
        conn = DriverManager.getConnection(getDbConfig(), getUser(), getPass());
    }
    public void
    createTable(String query) throws SQLException {
        pStmt = conn.prepareStatement(query);
        pStmt.execute();
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
}
