package controller;

import view.MainGui;

import java.sql.SQLException;

public class AppController {

    private MainGui         gui;
    private DBController    dbCtrl;


    public AppController(MainGui gui,DBController dbCtrl) {
        //attach gui & the DBController
        setGui(gui);
        setDbCtrl(dbCtrl);

        //pass the gui a reference to this ctrlr (mvc)
        getGui().setAppCtrl(this);
    }

    public DBController getDbCtrl() {
        return dbCtrl;
    }

    public MainGui getGui() {
        return gui;
    }

    public void setGui(MainGui gui) {
        this.gui = gui;
    }

    public void setDbCtrl(DBController dbCtrl) {
        this.dbCtrl = dbCtrl;
    }

    //create index and posting files from the storage
    public void tagSourceFiles() throws SQLException {
        getDbCtrl().tagAllSourceFiles();
    }
}
