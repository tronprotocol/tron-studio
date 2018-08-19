package org.tron.studio.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXDialogLayout;
import com.sun.javafx.util.Utils;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.tron.studio.MainApplication;

public class TopController {
    public Pane titlePane;
    private double xOffset;
    private double yOffset;
    private double xInit;
    private double yInit;
    private Rectangle2D backupWindowBounds;

    @PostConstruct
    public void initialize() throws IOException {

    }

    public void onClickSettings(MouseEvent mouseEvent) {
        JFXDialog dialog = new JFXDialog();
        dialog.setTransitionType(DialogTransition.CENTER);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setPrefWidth(800);
        layout.setHeading(new Label("Settings"));
        TextArea textArea = new TextArea();
        layout.setBody(textArea);
        dialog.setContent(layout);
        JFXButton closeButton = new JFXButton("OK");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> dialog.close());
        layout.setActions(closeButton);
        dialog.show((StackPane) MainApplication.instance.primaryStage.getScene().getRoot());
    }

    public void onClickAccount(MouseEvent mouseEvent) {
        JFXDialog dialog = new JFXDialog();
        dialog.setTransitionType(DialogTransition.CENTER);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setPrefWidth(800);
        layout.setHeading(new Label("Account"));
        TextArea textArea = new TextArea();
        layout.setBody(textArea);
        dialog.setContent(layout);
        JFXButton closeButton = new JFXButton("OK");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> dialog.close());
        layout.setActions(closeButton);
        dialog.show((StackPane) MainApplication.instance.primaryStage.getScene().getRoot());
    }

    public void onClickBlockChain(MouseEvent mouseEvent) {
        JFXDialog dialog = new JFXDialog();
        dialog.setTransitionType(DialogTransition.CENTER);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setPrefWidth(800);
        layout.setHeading(new Label("BlackChain Status"));
        TextArea textArea = new TextArea();
        layout.setBody(textArea);
        dialog.setContent(layout);
        JFXButton closeButton = new JFXButton("OK");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> dialog.close());
        layout.setActions(closeButton);
        dialog.show((StackPane) MainApplication.instance.primaryStage.getScene().getRoot());
    }


    private void dumpLayoutInfo(Node n, int depth) {
        for (int i = 0; i < depth; i++) System.out.print("  ");
        System.out.println(n);
        if (n instanceof Parent) {
            for (Node c : ((Parent) n).getChildrenUnmodifiable()) {
                dumpLayoutInfo(c, depth + 1);
            }
        }
    }

    public void dumpLayoutInfo(ActionEvent actionEvent) {
        dumpLayoutInfo(MainApplication.instance.primaryStage.getScene().getRoot(), 0);
    }
}
