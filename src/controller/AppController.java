package controller;

import annotations.A;
import model.DatabaseUtil;
import model.ParserUtil;
import view.MainGui;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;

public class AppController {

    private MainGui      gui;
    private DatabaseUtil db;
    private ParserUtil parser;


    public AppController(MainGui gui, DatabaseUtil db) {
        //attach gui & the DBController
        setGui(gui);
        setDb(db);

        //pass the gui a reference to this ctrlr (mvc)
        getGui().setAppCtrl(this);
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

    @A.DBOperation
    public String[] getAvailableSourceFiles()  {
        return db.getAvailableSourceFiles(db.getSourceFilesPath());
    }

    @A.DBOperation
    public String[] getLocalStorageFiles() throws SQLException {
        return db.getLocalStorageFiles();
    }


    @A.DBOperation
    @A.AdminOperation
    public void addFileToStorage(String fileName) throws FileNotFoundException, SQLException {
        if(false == db.isAlreadyInStorage(fileName)){
            System.out.println(fileName + " is a new file. adding to storage.");
            int docId = db.addFileToStorage(fileName);
            parser.indexFile(docId,fileName,db);
        }
        else {
            System.out.println(fileName + " is already in storage. set display=1");
            db.setFileAvailable(fileName);
        }


    }

    @A.DBOperation
    @A.AdminOperation
    public void removeFileFromStorage(String fileAndId) throws SQLException {
        db.removeFileFromStorage(fileAndId);
    }

    @A.Parsing
    public ArrayList<String[]> search(String searchQuery) throws SQLException {
        return parser.search(searchQuery,db);
    }

    @A.DBOperation
    @A.AdminOperation
    public String[] getAvailableStorageFiles() throws SQLException {
        return db.getAvailableStorageFiles(db.getLocalStoragePath());
    }


    public void attachParser(ParserUtil parser){
        this.parser = parser;
    }

    public ParserUtil getParser() {
        return parser;
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


    public void reset() throws SQLException {
        db.reset();
    }
}
