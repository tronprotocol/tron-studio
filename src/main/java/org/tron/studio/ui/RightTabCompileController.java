package org.tron.studio.ui;

import static org.tron.studio.solc.SolidityCompiler.Options.ABI;
import static org.tron.studio.solc.SolidityCompiler.Options.BIN;
import static org.tron.studio.solc.SolidityCompiler.Options.INTERFACE;
import static org.tron.studio.solc.SolidityCompiler.Options.METADATA;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXToggleButton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.studio.ShareData;
import org.tron.studio.solc.CompilationResult;
import org.tron.studio.solc.SolidityCompiler;

public class RightTabCompileController implements Initializable {
    static final Logger logger = LoggerFactory.getLogger(RightTabCompileController.class);

    public JFXComboBox contractComboBox;
    public JFXToggleButton autoCompileToggleButton;

    public RightTabCompileController() {
    }

    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    protected void onClickCompile() throws IOException {
        logger.debug("onClickCompile");

        String contractName = "/template/Ballot.sol";

        SolidityCompiler.Result res = SolidityCompiler.compile(
                new File(getClass().getResource(contractName).getPath()), true, ABI, BIN, INTERFACE, METADATA);
        logger.debug("Compile Out: '" + res.output + "'");
        logger.debug("Compile Err: '" + res.errors + "'");
        CompilationResult compilationResult = CompilationResult.parse(res.output);
        ShareData.setCompilationResult(contractName, compilationResult);

        List<String> contractNameList = new ArrayList<>();
        compilationResult.getContracts().forEach(contractResult -> {
            JSONObject metaData = JSON.parseObject(contractResult.metadata);
            JSONObject compilationTarget = metaData.getJSONObject("settings").getJSONObject("compilationTarget");
            compilationTarget.forEach((sol, value) -> {
                contractNameList.add((String) value);
            });
        });
        contractComboBox.setItems(FXCollections.observableArrayList(
                contractNameList
        ));
        contractComboBox.getSelectionModel().selectFirst();


        ShareData.currentContractName.set(contractName);
    }

    public void onClickAutoCompile(ActionEvent actionEvent) throws IOException {
        logger.debug("onClickAutoCompile {}", autoCompileToggleButton.isSelected());
    }

    public void onClickDetail(ActionEvent actionEvent) {
        logger.debug("onClickDetail {}", autoCompileToggleButton.isSelected());
    }
}