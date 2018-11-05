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
import javafx.fxml.FXMLLoader;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.ECKey;
import org.tron.core.Wallet;
import org.tron.studio.MainApplication;
import org.tron.studio.ShareData;
import org.tron.studio.utils.IPFieldValidator;
import org.tron.studio.utils.NetworkNameValidator;
import org.tron.studio.utils.PortFieldValidator;
import org.tron.studio.utils.PrivateKeyFieldValidator;

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
        layout.setHeading(new Label("Existing Settings"));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(20);
        ColumnConstraints columnConstraints0 = new ColumnConstraints();
        columnConstraints0.setHgrow(Priority.NEVER);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().add(columnConstraints0);
        gridPane.getColumnConstraints().add(columnConstraints1);

        int rownIdx = 0;
        List<JFXTextField> textfields = new ArrayList<>();
        for(String label : ShareData.saved_network.keySet()){
            Label title = new Label(label);
            JFXTextField ipTextField = new JFXTextField();
            JFXTextField portTextField = new JFXTextField();
            ipTextField.setText(ShareData.saved_network.get(label).url);
            portTextField.setText(Integer.toString(ShareData.saved_network.get(label).port));

            gridPane.add(title, 0, rownIdx);
            gridPane.add(ipTextField, 1, rownIdx);
            gridPane.add(portTextField, 2, rownIdx);

            ipTextField.setValidators(new IPFieldValidator());
            portTextField.setValidators(new PortFieldValidator());

            ipTextField.textProperty().addListener((o, oldVal, newVal) -> {
                if(ipTextField.validate())
                    ShareData.saved_network.get(label).url = newVal.trim();

            });
            portTextField.textProperty().addListener((o, oldVal, newVal) -> {
                if (portTextField.validate())
                    ShareData.saved_network.get(label).port = Integer.parseInt(newVal.trim());

            });
            textfields.add(ipTextField);
            textfields.add(portTextField);
            ++rownIdx;


        }

        layout.setBody(gridPane);
        dialog.setContent(layout);
        JFXButton closeButton = new JFXButton("OK");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> {
            try {


                for (int i=0; i < textfields.size(); ++i){
                    if (!textfields.get(i).validate()) return;
                }

                ShareData.currentRpcIp = ShareData.saved_network.get(ShareData.currentEnvironment).url;
                ShareData.currentRpcPort = ShareData.saved_network.get(ShareData.currentEnvironment).port;



            } catch (Exception e) {
                logger.error("Failed: {}", e);
                return;
            }
            dialog.close();
        });
//        testNetIpTextField.requestFocus();
        closeButton.setDefaultButton(true);
        layout.setActions(closeButton);
        dialog.show((StackPane) MainApplication.instance.primaryStage.getScene().getRoot());
    }

    public void onClickAccount(MouseEvent mouseEvent) {
        JFXDialog dialog = new JFXDialog();
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setPrefWidth(800);
        layout.setHeading(new Label("Import Account"));
        JFXTextField privateKeyTextField = new JFXTextField();
        privateKeyTextField.setPromptText("Paste your private key in hex format");
        privateKeyTextField.setValidators(new PrivateKeyFieldValidator());
        layout.setBody(privateKeyTextField);
        dialog.setContent(layout);
        JFXButton closeButton = new JFXButton("OK");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> {
            try {
                if (!privateKeyTextField.validate()) {
                    return;
                }
                ECKey ecKey = ECKey.fromPrivate(Hex.decode(privateKeyTextField.getText()));
                ShareData.testAccount.put(Wallet.encode58Check(ecKey.getAddress()), privateKeyTextField.getText());
                ShareData.newAccount.set(Wallet.encode58Check(ecKey.getAddress()));
                ShareData.currentAccount = Wallet.encode58Check(ecKey.getAddress());
            } catch (Exception e) {
                logger.error("Failed: {}", e);
                return;
            }
            dialog.close();
        });
        privateKeyTextField.resetValidation();
        closeButton.setDefaultButton(true);
        layout.setActions(closeButton);
        dialog.show((StackPane) MainApplication.instance.primaryStage.getScene().getRoot());
    }

    public void onAddSettings(MouseEvent mouseEvent){
        JFXDialog dialog = new JFXDialog();
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setPrefWidth(800);
        layout.setHeading(new Label("Add New Setting"));

        List<JFXTextField> fields = new ArrayList<>();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(20);
        ColumnConstraints columnConstraints0 = new ColumnConstraints();
        columnConstraints0.setHgrow(Priority.NEVER);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().add(columnConstraints0);
        gridPane.getColumnConstraints().add(columnConstraints1);

        Label title = new Label("Network Name");
        gridPane.add(title, 0, 0);
        JFXTextField nameField = new JFXTextField();
        gridPane.add(nameField, 1, 0);
        nameField.setValidators(new NetworkNameValidator());
        nameField.textProperty().addListener((o, oldVal, newVal) -> {
            nameField.validate();
        });
        fields.add(nameField);

        Label ip = new Label("Network URL");
        gridPane.add(ip, 0, 1);
        JFXTextField ipField = new JFXTextField();
        gridPane.add(ipField, 1, 1);
        ipField.setValidators(new IPFieldValidator());
        ipField.textProperty().addListener((o, oldVal, newVal) -> {
            ipField.validate();
        });
        fields.add(ipField);

        Label port = new Label("Network Port");
        gridPane.add(port, 0, 2);
        JFXTextField portField = new JFXTextField();
        gridPane.add(portField, 1, 2);
        portField.setValidators(new PortFieldValidator());
        portField.textProperty().addListener((o, oldVal, newVal) -> {
            portField.validate();
        });
        fields.add(portField);

        layout.setBody(gridPane);
        dialog.setContent(layout);

        JFXButton closeButton = new JFXButton("Add");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> {
            try {


                for (int i=0; i < fields.size(); ++i){
                    if (!fields.get(i).validate()) return;
                }

                ShareData.saved_network.put(nameField.getText().trim(), new ShareData.NetWorkEnvironment(ipField.getText().trim(),
                        Integer.parseInt(portField.getText().trim())));

                String output = String.format("%s:%s:%s", nameField.getText().trim(), ipField.getText().trim(), portField.getText().trim());
                ShareData.newNetwork.set(output);



            } catch (Exception e) {
                logger.error("Failed: {}", e);
                return;
            }

            dialog.close();
        });
        closeButton.setDefaultButton(true);
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
        textArea.requestFocus();
        closeButton.setDefaultButton(true);
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
