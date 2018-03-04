package view;

import controller.DBController;
import controller.AppController;

import javax.swing.*;

public class MainGui {

    private AppController   appCtrl;

    private JPanel mainFrame;

    public MainGui() {}

    public JPanel getMainFrame() {
        return mainFrame;
    }

    public void setAppCtrl(AppController guiCtrl) {
        this.appCtrl = guiCtrl;
    }
}
