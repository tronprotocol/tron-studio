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
        titlePane.setOnMousePressed(e -> {
            xOffset = e.getScreenX();
            yOffset = e.getScreenY();
            xInit = MainApplication.instance.primaryStage.getX();
            yInit = MainApplication.instance.primaryStage.getY();
        });
        titlePane.setOnMouseDragged(e -> {
            MainApplication.instance.primaryStage.setX(xInit + e.getScreenX() - xOffset);
            MainApplication.instance.primaryStage.setY(yInit + e.getScreenY() - yOffset);
        });
    }

    public void onClickMin(MouseEvent mouseEvent) {
        MainApplication.instance.primaryStage.setIconified(true);
    }

    public void onClickMax(MouseEvent mouseEvent) {
        Stage stage = MainApplication.instance.primaryStage;
        final double stageY = Utils.isMac() ? stage.getY() - 22 : stage.getY(); // TODO Workaround for RT-13980
        final Screen screen = Screen.getScreensForRectangle(stage.getX(), stageY, 1, 1).get(0);      // line 42
        Rectangle2D bounds = screen.getVisualBounds();
        if (bounds.getMinX() == stage.getX() && bounds.getMinY() == stageY &&
                bounds.getWidth() == stage.getWidth() && bounds.getHeight() == stage.getHeight()) {
            if (backupWindowBounds != null) {
                stage.setX(backupWindowBounds.getMinX());
                stage.setY(backupWindowBounds.getMinY());
                stage.setWidth(backupWindowBounds.getWidth());
                stage.setHeight(backupWindowBounds.getHeight());
            }
        } else {
            backupWindowBounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            final double newStageY = Utils.isMac() ? screen.getVisualBounds().getMinY() + 22 : screen.getVisualBounds().getMinY(); // TODO Workaround for RT-13980
            stage.setX(screen.getVisualBounds().getMinX());
            stage.setY(newStageY);
            stage.setWidth(screen.getVisualBounds().getWidth());
            stage.setHeight(screen.getVisualBounds().getHeight());
        }
    }

    public void onClickClose(MouseEvent mouseEvent) {
        Platform.exit();
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
