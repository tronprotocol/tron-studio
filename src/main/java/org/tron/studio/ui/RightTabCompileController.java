package org.tron.studio.ui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.studio.MainApplication;
import org.tron.studio.ShareData;
import org.tron.studio.filesystem.SolidityFileUtil;
import org.tron.studio.solc.CompilationErrorResult;
import org.tron.studio.solc.CompilationResult;
import org.tron.studio.solc.SolidityCompiler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.tron.studio.solc.SolidityCompiler.Options.*;

public class RightTabCompileController implements Initializable {
    static final Logger logger = LoggerFactory.getLogger(RightTabCompileController.class);

    public JFXComboBox<String> contractComboBox;
    public JFXCheckBox autoCompileCheckBox;
    public JFXCheckBox enableOtimizeCheckBox;
    public JFXButton compileButton;
    public JFXListView<Label> compileResultInfoListView;

    private List<String> contractABI = new ArrayList<>();
    private List<String> contractNameList = new ArrayList<>();
    private List<String> contractBin = new ArrayList<>();

    private int currentContractIndex = -1;
    private boolean isCompiling;
    private String contractFileName;

    public void initialize(URL location, ResourceBundle resources) {
        isCompiling = false;
    }

    @FXML
    protected void onClickCompile() {
        logger.debug("onClickCompile");
        contractComboBox.requestFocus();
        if (isCompiling) {
            return;
        }
        isCompiling = true;
        contractFileName = ShareData.currentContractFileName.getValue();
        ShareData.currentSolidityCompilerResult.set(null);
        new Thread(() -> {
            boolean compileSuccess = true;
            try {
                SolidityCompiler.Result solidityCompilerResult = SolidityCompiler.compile(
                        SolidityFileUtil.getExistFile(contractFileName), true, ABI, BIN, HASHES, INTERFACE,
                        METADATA);

                Platform.runLater(() -> ShareData.setSolidityCompilerResult(contractFileName, solidityCompilerResult));
                CompilationErrorResult.parse(solidityCompilerResult.errors);

                //There are errors
                if (!CompilationErrorResult.getErrors().isEmpty()) {
                    compileSuccess = false;
                    Platform.runLater(() -> {
                        compileResultInfoListView.getItems().clear();
                        CompilationErrorResult.getErrors().forEach(infoList -> {
                            Label text = new Label(infoList);
                            text.getStyleClass().add("compile-error-label");
                            String[] infos = infoList.split("\n");
                            int lines = infos.length;
                            text.setPrefHeight(lines * 30);
                            compileResultInfoListView.getItems().add(text);
                            text.maxWidthProperty().bind(compileResultInfoListView.widthProperty().subtract(40));
                        });
                        CompilationErrorResult.getWarnings().forEach(infoList -> {
                            Label text = new Label(infoList);
                            text.getStyleClass().add("compile-warn-label");
                            String[] infos = infoList.split("\n");
                            int lines = infos.length;
                            text.setPrefHeight(lines * 30);
                            compileResultInfoListView.getItems().add(text);
                            text.maxWidthProperty().bind(compileResultInfoListView.widthProperty().subtract(40));
                        });
                    });
                    return;
                } else {
                    CompilationResult compilationResult = CompilationResult.parse(solidityCompilerResult.output);

                    contractNameList.clear();
                    contractBin.clear();
                    contractABI.clear();
                    compilationResult.getContracts().forEach(contractResult -> {
                        contractBin.add(contractResult.bin);
                        contractABI.add(contractResult.abi);
                        JSONObject metaData = JSON.parseObject(contractResult.metadata);
                        JSONObject compilationTarget = metaData.getJSONObject("settings").getJSONObject("compilationTarget");
                        compilationTarget.forEach((sol, value) -> {
                            contractNameList.add((String) value);
                        });
                    });

                    Platform.runLater(() -> {

                        contractComboBox.setItems(FXCollections.observableArrayList(
                                contractNameList
                        ));

                        compileResultInfoListView.getItems().clear();
                        CompilationErrorResult.getWarnings().forEach(infoList -> {
                            Label text = new Label(infoList);
                            text.getStyleClass().add("compile-warn-label");
                            String[] infos = infoList.split("\n");
                            int lines = infos.length;
                            text.setPrefHeight(lines * 30);
                            compileResultInfoListView.getItems().add(text);
                            text.maxWidthProperty().bind(compileResultInfoListView.widthProperty().subtract(40));
                        });
                        contractNameList.forEach(contractName -> {
                            Label text = new Label(contractName);
                            text.getStyleClass().add("compile-succ-label");
                            text.setPrefHeight(40);
                            compileResultInfoListView.getItems().add(text);
                            text.maxWidthProperty().bind(compileResultInfoListView.widthProperty().subtract(40));
                        });
                    });
                }

            } catch (IOException e) {
                logger.error("Build failed: {} {}", e.getMessage(), e);
            } finally {
                if (compileSuccess) {
                    Platform.runLater(() -> {
                        contractComboBox.getSelectionModel().selectFirst();
                    });
                }
                isCompiling = false;
            }
        }).start();
    }

    public void onClickAutoCompile(ActionEvent actionEvent) throws IOException {
        ShareData.isAutoCompile.set(autoCompileCheckBox.isSelected());
    }

    public void onClickDetail(ActionEvent actionEvent) {
        if (currentContractIndex == -1) {
            return;
        }
        JFXDialog dialog = new JFXDialog();
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setPrefWidth(800);
        layout.setHeading(new Label("Detail"));

        VBox bodyVBox = new VBox();
        bodyVBox.setSpacing(5);
        TextArea abiTextArea = new TextArea();
        TextArea bytecodeTextArea = new TextArea();
        abiTextArea.setEditable(false);
        abiTextArea.setWrapText(true);
        bytecodeTextArea.setEditable(false);
        bytecodeTextArea.setWrapText(true);
        abiTextArea.setText(contractABI.get(currentContractIndex));
        bytecodeTextArea.setText(contractBin.get(currentContractIndex));
        bodyVBox.getChildren().add(new Label("ABI:"));
        bodyVBox.getChildren().add(abiTextArea);
        bodyVBox.getChildren().add(new Label("ByteCode:"));
        bodyVBox.getChildren().add(bytecodeTextArea);

        layout.setBody(bodyVBox);
        dialog.setContent(layout);
        JFXButton closeButton = new JFXButton("OK");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> dialog.close());
        layout.setActions(closeButton);
        dialog.show((StackPane) MainApplication.instance.primaryStage.getScene().getRoot());
    }

    public void onSelectContract(ActionEvent actionEvent) {
        currentContractIndex = contractComboBox.getSelectionModel().getSelectedIndex();
    }

    public void onClickEnableOptimize(ActionEvent actionEvent) {
        ShareData.enableOptimize = enableOtimizeCheckBox.isSelected();
    }
}