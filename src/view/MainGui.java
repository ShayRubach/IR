package view;

import controller.AppController;

import javax.swing.*;
import java.awt.*;

public class MainGui {

    private AppController   appCtrl;

    private JPanel mainPanel;
    private JFrame mainFrame;

    public MainGui() {
        initMainFrame();

    }

    private void initMainFrame() {
        mainFrame = new JFrame("IR System - by team_pwnz (c) ");
        //mainFrame.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize()));
        mainFrame.setSize(1124, 575);
        mainFrame.setContentPane(this.getMainPanel());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
        //mainPanel.setBounds(100, 100, 1124, 575);

    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAppCtrl(AppController guiCtrl) {
        this.appCtrl = guiCtrl;
    }
}
