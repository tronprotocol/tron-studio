package org.tron.studio.ui;

import static org.tron.studio.solc.SolidityCompiler.Options.ABI;
import static org.tron.studio.solc.SolidityCompiler.Options.BIN;
import static org.tron.studio.solc.SolidityCompiler.Options.INTERFACE;
import static org.tron.studio.solc.SolidityCompiler.Options.METADATA;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.studio.ShareData;
import org.tron.studio.solc.CompilationResult;
import org.tron.studio.solc.SolidityCompiler;

public class RightTabCompileController implements Initializable {
    static final Logger logger = LoggerFactory.getLogger(RightTabCompileController.class);

    public JFXComboBox contractComboBox;
    public JFXCheckBox autoCompileCheckBox;
    public JFXButton compileButton;

    List<String> contractABI = new ArrayList<>();
    List<String> contractNameList = new ArrayList<>();
    List<String> contractBin = new ArrayList<>();

    int currentContractIndex = -1;

    public RightTabCompileController() {
    }

    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    protected void onClickCompile() {
        logger.debug("onClickCompile");
        compileButton.setDisable(true);
        contractComboBox.requestFocus();
        ShareData.currentContractFileName.set(null);

        String contractFileName = "/template/Ballot.sol";
        new Thread(() -> {
            try {
                SolidityCompiler.Result res = SolidityCompiler.compile(
                    new File(getClass().getResource(contractFileName).getPath()), true, ABI, BIN, INTERFACE,
                    METADATA);

                logger.debug("Compile Out: '" + res.output + "'");
                logger.debug("Compile Err: '" + res.errors + "'");
                CompilationResult compilationResult = CompilationResult.parse(res.output);
                ShareData.setCompilationResult(contractFileName, compilationResult);

                contractNameList.clear();
                compilationResult.getContracts().forEach(contractResult -> {
                    contractBin.add(contractResult.bin);
                    contractABI.add(contractResult.abi);
                    JSONObject metaData = JSON.parseObject(contractResult.metadata);
                    JSONObject compilationTarget = metaData.getJSONObject("settings").getJSONObject("compilationTarget");
                    compilationTarget.forEach((sol, value) -> {
                        contractNameList.add((String) value);
                    });
                });
                contractComboBox.setItems(FXCollections.observableArrayList(
                    contractNameList
                ));

            } catch (IOException e) {
                logger.error("Build failed: {} {}", e.getMessage(), e);
                return;
            } finally {
                Platform.runLater(() -> {
                  contractComboBox.getSelectionModel().selectFirst();
                  ShareData.currentContractFileName.set(contractFileName);
                  compileButton.setDisable(false);
                });
            }
        }).start();
    }

    public void onClickAutoCompile(ActionEvent actionEvent) throws IOException {
        logger.debug("onClickAutoCompile {}", autoCompileCheckBox.isSelected());
    }

    public void onClickDetail(ActionEvent actionEvent) {
        logger.debug("onClickDetail {}", autoCompileCheckBox.isSelected());

        if (currentContractIndex == -1) {
            return;
        }

        Button btn = (Button)actionEvent.getSource();

        JFXAlert alert = new JFXAlert((Stage) btn.getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label(contractNameList.get(currentContractIndex)));

        String msg = "ABI\n";
        msg += contractABI.get(currentContractIndex);
        msg += "\nbytecode\n";
        msg += contractBin.get(currentContractIndex);

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(msg);

        layout.setBody(textArea);
        JFXButton closeButton = new JFXButton("ACCEPT");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> alert.hideWithAnimation());
        layout.setActions(closeButton);
        alert.setContent(layout);
        alert.show();
    }

    public void onSelectContract(ActionEvent actionEvent) {
        currentContractIndex = contractComboBox.getSelectionModel().getSelectedIndex();
    }
}