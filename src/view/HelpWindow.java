package view;

import javax.swing.*;
import java.awt.*;

public class HelpWindow {
    private JTextArea taHelpInformation;
    private JPanel mainWindow;
    private JFrame mainFrame;


    public HelpWindow() {

        System.out.println("CREATED HELP WINDOW");
        initWindow();
    }

    private void initWindow() {
        mainFrame = new JFrame("Help (?)");
        mainFrame.setSize(360,800);
        mainFrame.setContentPane(this.getMainWindow());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);

        //center window
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = 0;
        int y = (int) ((dimension.getHeight() - mainFrame.getHeight()) / 2);
        mainFrame.setLocation(x, y);
    }

    public JPanel getMainWindow() {
        return mainWindow;
    }

    public JFrame getMainFrame() {
        return mainFrame;
    }
}
