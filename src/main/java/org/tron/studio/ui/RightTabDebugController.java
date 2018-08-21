package org.tron.studio.ui;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXListView;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import com.jfoenix.controls.*;
import javafx.scene.layout.StackPane;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import org.tron.studio.ShareData;

public class RightTabDebugController implements Initializable {

    public JFXListView debugList;
    public boolean showedDebugList = false;

    public RightTabDebugController() {

    }

    public void initialize(URL location, ResourceBundle resources) {
        ShareData.debugTransactionAction.addListener((observable, oldValue, newValue) -> {
            if (showedDebugList)
            {
                return;
            }

            String[] nodes = {"Instructions", "Solidity Locals",
                    "Solidity State", "Step detail", "Stack",
                    "Storage completely loaded", "Memory", "Call Data", "Call Stack",
                    "Return Value", "Full Storages Changes\n"};
            String[] labels = {"test"};

            for (String nodename: nodes
            ) {
                JFXListView<Object> subList = createList(labels, nodename);
                debugList.getItems().add(subList);
            }

            showedDebugList = true;
        });
    }

    public void onClickDebug(ActionEvent actionEvent) {
    }

    private JFXListView<Object> createList(String[] labels, String groundName)
    {
        JFXListView<Object> subList = new JFXListView<>();

        for (String labelText:
             labels) {
            subList.getItems().add(new Label(labelText));
        }

        Node node = new HBox();
        ((HBox) node).getChildren().add(new Label(groundName));
        JFXRippler ripper = new JFXRippler();
        ripper.setStyle(":cons-rippler1");
        ripper.setPosition(JFXRippler.RipplerPos.BACK);

        StackPane pane = new StackPane();
        pane.setStyle(":-fx-padding: 2;");

        MaterialDesignIconView copyIcon = new MaterialDesignIconView();
        copyIcon.setGlyphName("CONTENT_COPY");
        copyIcon.setStyleClass("icon");
        pane.getChildren().add(copyIcon);

        ripper.getChildren().add(pane);

        ((HBox) node).getChildren().add(ripper);
        subList.setGroupnode(node);

        return subList;
    }

    public void onClickPlay(MouseEvent mouseEvent) {

    }

    public void onClickStop(MouseEvent mouseEvent) {
    }

    public void onClickStepOverBack(MouseEvent mouseEvent) {
    }

    public void onClickStepBack(MouseEvent mouseEvent) {
    }

    public void onClickStepInto(MouseEvent mouseEvent) {
    }

    public void onClickOverForward(MouseEvent mouseEvent) {
    }
}