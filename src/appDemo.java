
import controller.AppController;
import model.DatabaseUtil;
import model.ParserUtil;
import model.QueryUtil;
import view.MainGui;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public class appDemo {

    public static void main(String[] args) throws FileNotFoundException {

        String localStoragePath = "./local_storage";
        String sourceFilesPath  = "./source_files";
        String stopListPath  = "./src/view/StopList.txt";


        MainGui         mainGui = new MainGui();
        DatabaseUtil    dbUtil  = new DatabaseUtil(sourceFilesPath,localStoragePath);
        ParserUtil      parser = new ParserUtil(stopListPath);
        AppController   appCtrl = new AppController(mainGui,dbUtil);


        try {
            appCtrl.attachParser(parser);
            appCtrl.connect();
            appCtrl.createTable(QueryUtil.CREATE_STORAGE_FILES_TABLE);
            appCtrl.createTable(QueryUtil.CREATE_INDEX_TABLE);

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
