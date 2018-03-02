package controller;

import model.DatabaseInfo;
import model.QueryHolder;

import java.sql.*;

//Singleton
public class DBController {


    private DatabaseInfo db;
    private Connection conn;
    public  PreparedStatement pStmt;
    public  ResultSet rs;

    public DBController(DatabaseInfo db) {
        this.db = db;
    }

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName(db.getDbDriver());
        conn = DriverManager.getConnection(db.getDbConfig(), db.getUser(), db.getPass());
    }

    public void createTable(String query) throws SQLException {
        pStmt = conn.prepareStatement(queryd);
    }




}
