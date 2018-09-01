package org.tron.studio.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.tron.studio.MainApplication;
import org.tron.studio.ShareData;

@Slf4j(topic = "TopController")
public class TopController {
    public Pane titlePane;
    private double xOffset;
    private double yOffset;
    private double xInit;
    private double yInit;
    private Rectangle2D backupWindowBounds;

    public void onClickSettings(MouseEvent mouseEvent) {
        JFXDialog dialog = new JFXDialog();
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setPrefWidth(800);
        layout.setHeading(new Label("Settings"));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        ColumnConstraints columnConstraints0 = new ColumnConstraints();
        columnConstraints0.setHgrow(Priority.NEVER);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().add(columnConstraints0);
        gridPane.getColumnConstraints().add(columnConstraints1);

        Label localTitle = new Label("Local TVM");
        Label testNetTitle = new Label("Test Net");
        Label mainNetTitle = new Label("Main Net");

        JFXTextField localIpTextField = new JFXTextField();
        JFXTextField localPortTextField = new JFXTextField();
        localIpTextField.setText(ShareData.localRpcIp);
        localPortTextField.setText(String.valueOf(ShareData.localRpcPort));
        localIpTextField.setDisable(true);
        localPortTextField.setDisable(true);

        JFXTextField testNetIpTextField = new JFXTextField();
        JFXTextField testNetPortTextField = new JFXTextField();
        testNetIpTextField.setText(ShareData.testNetRpcIp);
        testNetPortTextField.setText(String.valueOf(ShareData.testNetRpcPort));

        JFXTextField mainNetIpTextField = new JFXTextField();
        JFXTextField mainNetPortTextField = new JFXTextField();
        mainNetIpTextField.setText(ShareData.mainNetRpcIp);
        mainNetPortTextField.setText(String.valueOf(ShareData.mainNetRpcPort));

        gridPane.add(localTitle, 0, 0);
        gridPane.add(localIpTextField, 1, 0);
        gridPane.add(localPortTextField, 2, 0);

        gridPane.add(testNetTitle, 0, 1);
        gridPane.add(testNetIpTextField, 1, 1);
        gridPane.add(testNetPortTextField, 2, 1);

        gridPane.add(mainNetTitle, 0, 2);
        gridPane.add(mainNetIpTextField, 1, 2);
        gridPane.add(mainNetPortTextField, 2, 2);

        layout.setBody(gridPane);
        dialog.setContent(layout);
        JFXButton closeButton = new JFXButton("OK");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> {
            try {
                ShareData.testNetRpcIp = testNetIpTextField.getText();
                ShareData.testNetRpcPort = Integer.parseInt(testNetPortTextField.getText());
                ShareData.mainNetRpcIp = mainNetIpTextField.getText();
                ShareData.mainNetRpcPort = Integer.parseInt(mainNetPortTextField.getText());
            } catch (Exception e) {
                logger.error("Failed: {}", e);
            }
            dialog.close();
        });
        layout.setActions(closeButton);
        dialog.show((StackPane) MainApplication.instance.primaryStage.getScene().getRoot());
    }

    public void onClickAccount(MouseEvent mouseEvent) {
        JFXDialog dialog = new JFXDialog();
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
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setPrefWidth(800);
        layout.setHeading(new Label("Blockchain Status"));
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
