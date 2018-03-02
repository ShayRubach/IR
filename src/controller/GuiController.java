package controller;

import model.DatabaseInfo;
import view.MainGui;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GuiController {

    private MainGui gui;

    public GuiController(MainGui gui) {
        this.gui = gui;
    }

    public MainGui getGui() {
        return gui;
    }
}
