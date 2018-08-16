package org.tron.studio.ui;

import static org.tron.studio.solc.SolidityCompiler.Options.ABI;
import static org.tron.studio.solc.SolidityCompiler.Options.BIN;
import static org.tron.studio.solc.SolidityCompiler.Options.INTERFACE;
import static org.tron.studio.solc.SolidityCompiler.Options.METADATA;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXToggleButton;
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
import javafx.scene.control.TextArea;
import javafx.stage.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.studio.ShareData;
import org.tron.studio.solc.CompilationResult;
import org.tron.studio.solc.SolidityCompiler;

public class RightTabCompileController implements Initializable {
    static final Logger logger = LoggerFactory.getLogger(RightTabCompileController.class);

    public JFXComboBox contractComboBox;
    public JFXToggleButton autoCompileToggleButton;
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

        String contractName = "/template/Ballot.sol";
        new Thread(() -> {
            try {
                SolidityCompiler.Result res = SolidityCompiler.compile(
                    new File(getClass().getResource(contractName).getPath()), true, ABI, BIN, INTERFACE,
                    METADATA);

                logger.debug("Compile Out: '" + res.output + "'");
                logger.debug("Compile Err: '" + res.errors + "'");
                CompilationResult compilationResult = CompilationResult.parse(res.output);
                ShareData.setCompilationResult(contractName, compilationResult);

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
                  ShareData.currentContractName.set(contractName);
                  compileButton.setDisable(false);
                });
            }
        }).start();
    }

    public void onClickAutoCompile(ActionEvent actionEvent) throws IOException {
        logger.debug("onClickAutoCompile {}", autoCompileToggleButton.isSelected());
    }

    private  Popup createPopup() throws IOException {
        //FXMLLoader loader = new FXMLLoader();
        //loader.setLocation(Class.class
        //        .getResource("application.fxml"));
        //page = (AnchorPane) loader.load();
        //page = (AnchorPane)loader.load(getClass().getResource("application.fxml"));

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(40);
        textArea.setPrefColumnCount(50);

        textArea.setText(contractNameList.get(currentContractIndex));
        textArea.appendText("\n");
        textArea.appendText("abi");
        textArea.appendText("\n");
        textArea.appendText(contractABI.get(currentContractIndex));
        textArea.appendText("\n");
        textArea.appendText("bytecode");
        textArea.appendText("\n");
        textArea.appendText(contractBin.get(currentContractIndex));


        final Popup popup = new Popup();

        popup.setAutoHide(true);
        //popup.setX(300);
        //popup.setY(200);
        popup.getContent().addAll(textArea);
        //popup.getContent().addAll(new Circle(25, 25, 50, Color.AQUAMARINE));
        return popup;
    }

    public void onClickDetail(ActionEvent actionEvent) {
        logger.debug("onClickDetail {}", autoCompileToggleButton.isSelected());

        if (currentContractIndex == -1) {
            return;
        }

        Button btn = (Button)actionEvent.getSource();
        Popup popup = new Popup();
        try {
            popup = createPopup();
        } catch(IOException e)
        {
            logger.error("Load failed");
        }

        popup.show(btn.getScene().getWindow());
    }

    public void onSelectContract(ActionEvent actionEvent) {
        currentContractIndex = contractComboBox.getSelectionModel().getSelectedIndex();
    }
}