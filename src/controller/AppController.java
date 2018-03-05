package controller;

import model.DatabaseUtil;
import view.MainGui;

import java.sql.SQLException;

public class AppController {

    private MainGui         gui;
    private DatabaseUtil db;


    public AppController(MainGui gui, DatabaseUtil db) {
        //attach gui & the DBController
        setGui(gui);
        setDb(db);

        //pass the gui a reference to this ctrlr (mvc)
        getGui().setAppCtrl(this);
    }

    public DatabaseUtil getDb() {
        return db;
    }

    public MainGui getGui() {
        return gui;
    }

    public void setGui(MainGui gui) {
        this.gui = gui;
    }

    public void setDb(DatabaseUtil db) {
        this.db = db;
    }

    //create index and posting files from the storage
    public void tagAllSourceFiles() throws SQLException {
        getDb().tagAllSourceFiles();
    }

    public boolean isLoggedAsAdmin() {
        return db.isLoggedAsAdmin();
    }

    public void connect() throws SQLException, ClassNotFoundException {
        getDb().connect();
    }

    public void createTable(String query) throws SQLException {
        getDb().createTable(query);
    }

    public boolean verifyAdminPass(String pass) {

        boolean result = false;
        if(getDb().verifyAdminPass(pass))
            result = true;

        return result;
    }

    public void setAdminAccess(boolean access) {
        db.setAdminAccess(access);
    }

    public String[] getAvailableSourceFiles() throws SQLException {
        return db.getAvailableSourceFiles();
    }
}
