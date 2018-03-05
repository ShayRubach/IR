
import controller.AppController;
import model.DatabaseInfo;
import model.QueryHolder;
import view.MainGui;

import java.sql.SQLException;

public class appDemo {

    public static void main(String[] args) {

        String localStoragePath = "./local_storage";
        String sourceFilesPath  = "./source_files";

        MainGui         mainGui = new MainGui();
        DatabaseInfo    dbInfo = new DatabaseInfo(sourceFilesPath,localStoragePath);
        AppController   appCtrl = new AppController(mainGui,dbInfo);


        try {
            appCtrl.connect();
            appCtrl.createTable(QueryHolder.CREATE_FILE_TABLE);
            appCtrl.createTable(QueryHolder.CREATE_INDEX_TABLE);
            appCtrl.tagAllSourceFiles();

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
