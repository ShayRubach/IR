package view;

import controller.AppController;

import javax.swing.*;
import java.awt.*;

public class MainGui {

    private AppController   appCtrl;

    private JPanel mainPanel;
    private JTextArea taFullDocContent;
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
    private JFrame mainFrame;


    public static final String LOG_OUT = "Log Out";
    public static final String LOG_IN = "Log In";
    public static final String WRONG_PASSWORD = "Wrong password.";
    public static final String LOGGED_AS_VISITOR = "Logged as: Visitor";
    public static final String LOGGED_AS_ADMIN = "Logged as: Admin";

    private HelpWindow helpWindow = new HelpWindow();

    public MainGui() {
        initMainFrame();
        initButtons();
        initButtonListeners();
        initComboxBoxes();


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
        jcbDocNameResults.setEnabled(false);
        jcbAddDoc.setEnabled(false);
        jcbRemoveDoc.setEnabled(false);
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

                btnRemoveDoc.setEnabled(false);
                btnAddDoc.setEnabled(false);
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

                    btnRemoveDoc.setEnabled(true);
                    btnAddDoc.setEnabled(true);

                }
                else {
                    //wrong password
                    setLblSystemMsg(WRONG_PASSWORD);
                    getLblSystemMsg().setForeground(Color.RED);
                }

            }
            return;
        });


        //Search button:
        btnSearch.addActionListener(e -> {

            //get search terms and make a toLowerCase()
            //parse line
            //detect operator

        });




    }

    //TODO: implement this and keep the logic in gui
    public void loadDb(){
        //load db here
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




}
