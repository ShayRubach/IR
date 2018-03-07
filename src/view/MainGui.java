package view;

import annotations.A;
import controller.AppController;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private ArrayList<String[]> records;


    public static final String LOG_OUT = "Log Out";
    public static final String LOG_IN  = "Log In";
    public static final String WRONG_PASSWORD = "Wrong password.";
    public static final String LOGGED_AS_VISITOR = "Logged as: Visitor";
    public static final String LOGGED_AS_ADMIN = "Logged as: Admin";

    private HelpWindow helpWindow = new HelpWindow();

    public MainGui() throws FileNotFoundException {
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

    public void initComboxBoxesListeners() throws FileNotFoundException{

        jcbDocNameResults.addActionListener(e -> {
            if(jcbDocNameResults.getSelectedIndex() > 1){
                String path = fetchFilePath(jcbDocNameResults.getSelectedItem().toString());
                try {
                    //clear any content displayed before
                    taFullDocContent.setText("");
                    displayFileContent(path);

                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
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

    private void displayFileContent(String path) throws FileNotFoundException {
        Scanner itr = new Scanner(new File(path));
        String line = null;
        String word = null;
        StringBuilder fullFileContent = new StringBuilder();
        ArrayList<Integer> pos = new ArrayList<>();

        while(itr.hasNextLine()){
            line = itr.nextLine();
            fullFileContent.append(line.toLowerCase() + "\n");
            taFullDocContent.setText(taFullDocContent.getText() + "\n" + line);

            //TODO: change this to get word from another soruce (already parsed) and not the raw text from input
            word = tfSearchLine.getText();

            //get all word positions in line to later be highlighted:


        }
        pos = getWordPositions(pos,word,fullFileContent);
        System.out.println(pos);
        System.out.println(fullFileContent);

        //TODO: highlight the word here - do not forget multiple words case
        highlightInTextArea(taFullDocContent,word,line,pos);

    }

    private void highlightInTextArea(JTextArea taFullDocContent, String phrase, String line, ArrayList<Integer> pos) {
        Highlighter highlighter = taFullDocContent.getHighlighter();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);

        //int posStart ;
        //int posEnd = posStart + phrase.length();

        for(Integer posStart : pos) {
            //System.out.println("phrase:"+phrase + "," + "line:"+line + ":" + posStart+","+posEnd);
            try {
                highlighter.addHighlight(posStart+1, posStart+phrase.length()+1, painter);

            } catch (BadLocationException e) {
                System.out.println("highlightInTextArea called. bad location exception occurred.");
                e.printStackTrace();
            }
        }

    }

    private ArrayList<Integer> getWordPositions(ArrayList<Integer> pos, String word, StringBuilder line) {

        //regex to match exact phrase/word in line
        Matcher m = Pattern.compile("(?=(\\b"+ word + "\\b))").matcher(line);   // the \b is for exact word boundaries

        while (m.find())
            pos.add(m.start());

        return pos;
    }

    private String fetchFilePath(String s) {
        String path = null;
        String id = s.substring(s.indexOf("(")+1,s.indexOf(")"));

        for(String[] record : records){
            if(record[1].equals(id))
                path = record[4];
        }

        return path;
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
                    addFileToStorage(fileName);
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
                    removeFileFromStorage(fileAndId);
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
            defaultComboBoxHeader(jcbDocNameResults,"Result");
            if(/* some validation on string */ true){
                String searchQuery = getTfSearchLine().getText().toString();
                try {


                    records = appCtrl.search(searchQuery);
                    resetTableRecords();
                    loadRecordsIntoTable(records);

                    //TODO: show the found docs in jcb
                    displayDocsInComboBox(records);

                    //clear any content displayed before
                    taFullDocContent.setText("");
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }


        });




    }

    private void displayDocsInComboBox(ArrayList<String[]> records) {
        //get id and name

        for(String[] record : records){

            jcbDocNameResults.addItem(record[3] + " (" + record[1] +")");
        }
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
    public void removeFileFromStorage(String fileAndId) throws SQLException {

        if(/* some code */ true ){
            appCtrl.removeFileFromStorage(fileAndId);
        }

    }


    @A.AdminOperation
    @A.DBOperation
    public void addFileToStorage(String fileName) throws FileNotFoundException, SQLException {

        if(/* some code */ true ){
            appCtrl.addFileToStorage(fileName);
        }

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
