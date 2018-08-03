package org.tron.studio;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController implements Initializable {
    static final Logger logger = LoggerFactory.getLogger(MainController.class);
    public CodeArea codeArea;
    public HBox rootPanel;
    public TabPane codeAreaTabPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StringBuilder builder = new StringBuilder();
        System.out.println("MainController");
        try {
            Files.lines(Paths.get(getClass().getResource("/template/Ballot.sol").getPath())).forEach(line -> {
                builder.append(line).append(System.getProperty("line.separator"));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        codeArea.insertText(0, builder.toString());

        ShareData.newContractName.addListener((observable, oldValue, newValue) -> {
            try {
                Tab codeTab = FXMLLoader.load(getClass().getResource("ui/code_panel.fxml"));
                codeTab.setText(newValue);
                codeAreaTabPane.getTabs().add(codeTab);
                StringBuilder templateBuilder = new StringBuilder();
                templateBuilder.append("pragma solidity ^0.4.0;").append("\n");
                templateBuilder.append("contract ").append(newValue).append(" {").append("\n");
                templateBuilder.append("}").append("\n");
                CodeArea codeArea = (CodeArea) codeTab.getContent();
                codeArea.insertText(0, templateBuilder.toString());
                codeAreaTabPane.getSelectionModel().select(codeTab);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        });
    }

}
