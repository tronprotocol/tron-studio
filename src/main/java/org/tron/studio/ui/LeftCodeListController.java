package org.tron.studio.ui;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeftCodeListController {
    static final Logger logger = LoggerFactory.getLogger(RightTabCompileController.class);
    @FXML private TabPane codeAreaTabPane;
    @FXML private Tab defualtTab;

    @FXML
    public void initialize() {
        // Nothing
    }

    @FXML
    private void addTab() {
        logger.debug("addTab");
        System.out.println(codeAreaTabPane);
        int numTabs = codeAreaTabPane.getTabs().size();
        logger.debug(Integer.toString(numTabs));
        Tab tab = new Tab("Tab "+(numTabs+1));
        codeAreaTabPane.getTabs().add(tab);
    }

    @FXML
    private void listTabs() {
        codeAreaTabPane.getTabs().forEach(tab -> System.out.println(tab.getText()));
        System.out.println();
    }

    public void CreateContract(ActionEvent actionEvent) {
        //tabPane.getTabs().add(new Tab("Tab 1"))
        addTab();
        listTabs();
    }
}
