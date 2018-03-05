
import controller.AppController;
import model.DatabaseUtil;
import model.QueryUtil;
import view.MainGui;

import java.sql.SQLException;

public class appDemo {

    public static void main(String[] args) {

        String localStoragePath = "./local_storage";
        String sourceFilesPath  = "./source_files";

        MainGui         mainGui = new MainGui();
        DatabaseUtil dbInfo = new DatabaseUtil(sourceFilesPath,localStoragePath);
        AppController   appCtrl = new AppController(mainGui,dbInfo);


        try {
            appCtrl.connect();
            appCtrl.createTable(QueryUtil.CREATE_FILE_TABLE);
            appCtrl.createTable(QueryUtil.CREATE_INDEX_TABLE);
            appCtrl.tagAllSourceFiles();
            appCtrl.getGui().loadDbToApp();

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
