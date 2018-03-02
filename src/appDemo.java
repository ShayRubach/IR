import controller.DBController;
import controller.GuiController;
import model.DatabaseInfo;
import model.QueryHolder;
import view.MainGui;

import java.sql.SQLException;

public class appDemo {

    public static void main(String[] args) {

        DatabaseInfo    dbInfo = new DatabaseInfo();
        DBController    dbCtrl = new DBController(dbInfo);
        MainGui         mainGui = new MainGui();
        GuiController   guiCtrl = new GuiController(mainGui);

        try {

            dbCtrl.connect();
            dbCtrl.createTable(QueryHolder.CREATE_POSTING_TABLE);

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
