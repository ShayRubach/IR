package model;

public class DatabaseInfo {

    private String user = "root";
    private String pass = "";
    private String dbDriver = "com.mysql.jdbc.Driver";
    private String dbConfig = "jdbc:mysql://localhost:3306/IR?createDatabaseIfNotExist=true";

    public DatabaseInfo() {}

    public DatabaseInfo(String user, String pass, String dbDriver, String dbConfig) {
        this.user = user;
        this.pass = pass;
        this.dbDriver = dbDriver;
        this.dbConfig = dbConfig;
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
