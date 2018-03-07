package view;

import annotations.A;
import controller.AppController;
import model.ParserUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainGui {

    private AppController   appCtrl;

    private JPanel mainPanel;
    private JTable tableIndexDocResults;
    private JComboBox jcbDocNameResults;
    private JTextField tfSearchLine;
    private JButton btnSearch;
    private JComboBox jcbAddDoc;
    private JComboBox jcbRemoveDoc;
    private JButton btnAddDoc;
    private JButton btnRemoveDoc;
    private JPasswordField pfAdminPassword;
    private JButton btnLoginAsAdmin;
    private JLabel lblAppName;
    private JLabel lblLoggedAs;
    private JLabel lblSystemMsg;
    private JScrollPane jspDocResTable;
    private JTextArea taFullDocContent;
    private JFrame mainFrame;
    private DefaultTableModel modelIndexDocResults;


    public static final String LOG_OUT = "Log Out";
    public static final String LOG_IN  = "Log In";
    public static final String WRONG_PASSWORD = "Wrong password.";
    public static final String LOGGED_AS_VISITOR = "Logged as: Visitor";
    public static final String LOGGED_AS_ADMIN = "Logged as: Admin";

    private HelpWindow helpWindow = new HelpWindow();

    public MainGui() {
        initMainFrame();
        initButtons();
        initButtonListeners();
        initComboxBoxes();
        initComboxBoxesListeners();
        initTables();

        }

    private void initTables() {
        final String[] tbIndexDocResColumns = {"Word","Doc Id","Appearances"};
        modelIndexDocResults = new DefaultTableModel(null,tbIndexDocResColumns);
        tableIndexDocResults.setBackground(new Color(255,255,255));
        tableIndexDocResults.setModel(modelIndexDocResults);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );

        tableIndexDocResults.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableIndexDocResults.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableIndexDocResults.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

    }

    private void resetTableRecords(){
        modelIndexDocResults.setRowCount(0);
    }

    private void initMainFrame() {
        mainFrame = new JFrame("IR System - by team_pwnz (c) ");
        //mainFrame.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize()));
        mainFrame.setSize(1200,800);
        mainFrame.setContentPane(this.getMainPanel());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);

        //center window
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ( 150 + (dimension.getWidth() - mainFrame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - mainFrame.getHeight()) / 2);
        mainFrame.setLocation(x, y);

    }


    public void initButtons(){

        btnLoginAsAdmin.setText(LOG_IN);
        btnAddDoc.setEnabled(false);
        btnRemoveDoc.setEnabled(false);
    }

    public void initComboxBoxes(){

        jcbDocNameResults.setEnabled(true);
        jcbAddDoc.setEnabled(false);
        jcbRemoveDoc.setEnabled(false);

        defaultComboBoxHeader(jcbDocNameResults,"Result");
        defaultComboBoxHeader(jcbAddDoc,"Document");
        defaultComboBoxHeader(jcbRemoveDoc,"Document");

    }

    public void initComboxBoxesListeners(){

        jcbDocNameResults.addActionListener(e -> {
            if(jcbDocNameResults.getSelectedIndex() > 1){
                //TODO: load the results?
                if(jcbDocNameResults.getSelectedIndex() > 1){

                }
                else{
                    jcbDocNameResults.setSelectedIndex(0);
                }
            }
        });

        jcbAddDoc.addActionListener(e -> {
            if(jcbAddDoc.getSelectedIndex() > 1){
                btnAddDoc.setEnabled(true);
            }
            else
                btnAddDoc.setEnabled(false);

        });

        jcbRemoveDoc.addActionListener(e -> {
            if(jcbRemoveDoc.getSelectedIndex() > 1){
                btnRemoveDoc.setEnabled(true);
            }
            else
                btnRemoveDoc.setEnabled(false);

        });
    }

    private void initButtonListeners() {

        //Login/Logout button:
        btnLoginAsAdmin.addActionListener(e -> {

            if(appCtrl.isLoggedAsAdmin()){
                //already logged as admin
                setLblSystemMsg("");
                btnLoginAsAdmin.setText(LOG_IN);
                appCtrl.setAdminAccess(false);
                getPfAdminPassword().setEnabled(true);
                getPfAdminPassword().setBackground(Color.WHITE);
                setLblLoggedAs(LOGGED_AS_VISITOR);

                jcbAddDoc.setEnabled(false);
                jcbRemoveDoc.setEnabled(false);

            }
            else {
                //logged as visitor
                String pass = getPfAdminPassword().getText().toString();
                if(appCtrl.verifyAdminPass(pass)){
                    setLblLoggedAs(LOGGED_AS_ADMIN);
                    btnLoginAsAdmin.setText(LOG_OUT);
                    appCtrl.setAdminAccess(true);
                    getPfAdminPassword().setText("");
                    getPfAdminPassword().setEnabled(false);
                    getPfAdminPassword().setBackground(new Color(193, 196, 201));
                    setLblSystemMsg("Hello");
                    getLblSystemMsg().setForeground(new Color(13, 163, 8));

                    jcbAddDoc.setEnabled(true);
                    jcbRemoveDoc.setEnabled(true);

                    try {
                        loadDbToApp();
                    } catch (SQLException e1) {
                        System.out.println("loadDbToApp called. Could not load db.");
                        e1.printStackTrace();
                    }

                }
                else {
                    //wrong password
                    setLblSystemMsg(WRONG_PASSWORD);
                    getLblSystemMsg().setForeground(Color.RED);
                }

            }
            return;
        });


        //TODO: add a re-add an already hidden document
        //TODO: turn jcbAddDoc into JList for group selection of documents
        btnAddDoc.addActionListener(e -> {
            //make sure some document is chosen
            if(jcbAddDoc.getSelectedIndex() > 1){
                String fileName = jcbAddDoc.getSelectedItem().toString();
                try {
                    appCtrl.addFileToStorage(fileName);
                    loadDbToApp();
                    jcbAddDoc.setSelectedIndex(0);
                } catch (FileNotFoundException e1) {
                    System.out.println("addFileToStorage called. File not found");
                    e1.printStackTrace();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }


        });

        //TODO: turn jcbRemoveDoc into JList for group selection of documents
        btnRemoveDoc.addActionListener(e -> {
            //make sure some document is chosen
            if(jcbRemoveDoc.getSelectedIndex() > 1){
                String fileAndId = jcbRemoveDoc.getSelectedItem().toString();
                try {
                    //exclude file on search results
                    appCtrl.removeFileFromStorage(fileAndId);
                    loadDbToApp();
                    jcbRemoveDoc.setSelectedIndex(0);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });



        //Search button:
        btnSearch.addActionListener(e -> {

            //TODO: search event handling
            ArrayList<String[]> records = new ArrayList<>();
            if(/* some validation on string */ true){
                String searchQuery = getTfSearchLine().getText().toString();
                try {
                    defaultComboBoxHeader(jcbDocNameResults,"Document");
                    records = appCtrl.search(searchQuery);

                    resetTableRecords();
                    loadRecordsIntoTable(records);

                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }


        });




    }

    private void loadRecordsIntoTable(ArrayList<String[]> records) {
        for(String[] record : records){
            modelIndexDocResults.addRow(record);
        }

    }

    public void defaultComboBoxHeader(JComboBox jBox, String type){
        if(null != jBox){
            jBox.removeAllItems();
            jBox.addItem("Select " + type);
            jBox.addItem("");
        }
    }


    @A.DBOperation
    public void loadDbToApp() throws SQLException {

        String[] docList;
        String fixedSourceString = null;
        String delim = appCtrl.getDb().DELIM;

        //get all available source files and display them on associated combo box:
        docList = appCtrl.getAvailableSourceFiles();
        defaultComboBoxHeader(jcbAddDoc,"Document");
        for(String doc : docList){
            //doc = doc.substring(0,doc.length()-4);
            jcbAddDoc.addItem(doc);
        }

        //get all storage files and display them on associated combo box:
        docList = appCtrl.getLocalStorageFiles();
        defaultComboBoxHeader(jcbRemoveDoc,"Document");
        for(String s: docList)
            jcbRemoveDoc.addItem(s);


    }


    @A.AdminOperation
    @A.DBOperation
    public boolean removeDocument(int docID){
        boolean res = false;
        if(/* some code */ true ){

        }
        return res;
    }


    @A.AdminOperation
    @A.DBOperation
    public boolean addDocument(int docID){
        boolean res = false;
        if(/* some code */ true ){

        }
        return res;
    }



    public JPasswordField getPfAdminPassword() {
        return pfAdminPassword;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAppCtrl(AppController guiCtrl) {
        this.appCtrl = guiCtrl;
    }

    public JLabel getLblLoggedAs() {
        return lblLoggedAs;
    }

    public void setLblLoggedAs(String lblLoggedAs) {
        this.lblLoggedAs.setText(lblLoggedAs);
    }

    public JLabel getLblSystemMsg() {
        return lblSystemMsg;
    }

    public void setLblSystemMsg(String lblSystemMsg) {
        this.lblSystemMsg.setText(lblSystemMsg);
    }

    public JTextField getTfSearchLine() {
        return tfSearchLine;
    }
}
