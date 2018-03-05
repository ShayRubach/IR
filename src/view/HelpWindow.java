package view;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class HelpWindow {
    private JTextArea taHelpInformation;
    private JPanel mainWindow;
    private JFrame mainFrame;


    public HelpWindow() {
        initWindow();

        try {
            displayHelp();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayHelp() throws IOException {

        Path path = Paths.get("src/view/","Help.txt");
        Charset charset = Charset.forName("ISO-8859-1");

        ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(path, charset);

        for (String line : lines) {
            taHelpInformation.setText(taHelpInformation.getText() + "\n    " +  line.toString());
        }

        getTaHelpInformation().setEditable(false);
    }

    private void initWindow() {
        mainFrame = new JFrame("Help (?)");
        mainFrame.setSize(510,800);
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

    public JTextArea getTaHelpInformation() {
        return taHelpInformation;
    }
}
