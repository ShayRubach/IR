package view;

import controller.AppController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainGui {

    private AppController   appCtrl;

    private JPanel mainPanel;
    private JTextArea taFullDocContent;
    private JTable tableIndexDocResults;
    private JComboBox jcbDocNameResults;
    private JTextField tfSearchLine;
    private JButton searchButton;
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



    private HelpWindow helpWindow = new HelpWindow();

    public MainGui() {
        initMainFrame();
        initButtons();
        initButtonListeners();

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

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAppCtrl(AppController guiCtrl) {
        this.appCtrl = guiCtrl;
    }


    public void initButtons(){
        btnLoginAsAdmin.setEnabled(true);
        btnAddDoc.setEnabled(false);
        btnRemoveDoc.setEnabled(false);
    }


    private void initButtonListeners() {
        btnLoginAsAdmin.addActionListener(e -> {

            if(appCtrl.isLoggedAsAdmin()){

            }

        });
    }

}
