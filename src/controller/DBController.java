package controller;

import model.DatabaseInfo;

import java.sql.*;

//Singleton
public class DBController {

    private DatabaseInfo db;

    public DBController(DatabaseInfo db) {
        this.db = db;
    }

    public void connect() throws ClassNotFoundException, SQLException {
        db.connect();
    }

    public void createTable(String query) throws SQLException {
        db.createTable(query);
    }

    public void tagAllSourceFiles() throws SQLException {
        db.tagAllSourceFiles();
    }

    public boolean isLoggedAsAdmin() {
        return db.isLoggedAsAdmin();
    }
}
