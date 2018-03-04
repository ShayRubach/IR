import controller.DBController;
import controller.AppController;
import model.DatabaseInfo;
import model.QueryHolder;
import view.MainGui;

import java.sql.SQLException;

public class appDemo {

    public static void main(String[] args) {

        MainGui         mainGui = new MainGui();
        DatabaseInfo    dbInfo = new DatabaseInfo();
        DBController    dbCtrl = new DBController(dbInfo);
        AppController   appCtrl = new AppController(mainGui,dbCtrl);



        try {
            dbCtrl.connect();
            dbCtrl.createTable(QueryHolder.CREATE_FILE_TABLE);
            dbCtrl.createTable(QueryHolder.CREATE_INDEX_TABLE);

        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Failed to connect.");
        }
        catch (SQLException e2){
            e2.printStackTrace();
            System.out.println("SQL Exception.");
        }
    }
}
